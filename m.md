# Architecture

Spring Boot, Spring Data JPA (Hibernate), H2 in file mode (`jdbc:h2:file:`), schema in `schema.sql` (portable to Postgres). Money is `BigDecimal` / `DECIMAL(19,4)` (scale 4, never `double`); timestamps are `Instant` (UTC). Layers: controllers (HTTP only) -> services (rules, locking, snapshot) -> repositories (JPA).

## 1. `external_id` uniqueness and concurrent POSTs

- `transactions.external_id` carries a **database unique constraint** (`uq_txn_external_id`). The DB is the authority; application checks are only a fast path.
- **Duplicate policy: 409** with `{"error":"duplicate_external_id","existing_id":"txn_N"}`. The lookup runs before validation, so a retry with a different or invalid body still returns the existing id.
- Two overlapping POSTs both pass the lookup. One insert wins; the other violates the constraint, and the service catches it **outside** the failed transaction, looks up the winner's id and returns 409. Only a violation where the lookup finds a row is treated as a duplicate; other integrity errors surface.
- Header, tax lines, fee lines and metadata are written in **one DB transaction** (cascade), so a create is all or nothing.

## 2. Tables and models

`transactions` holds what every type shares: `id`, `external_id`, `type`, `currency`, `occurred_at`, `total`.

**Taxes and fees are separate child tables**, not one extras column: `transaction_tax_lines(name, rate, amount)` and `transaction_fee_lines(name, amount)`. They differ in shape (tax has a rate) and roll up differently (tax by name and rate, fees by name). Navan fee *transactions* are a type, not fee lines.

**Type-specific data uses composition with a shared primary key.** `transaction_metadata.transaction_id` is both PK and FK to `transactions.id` (1:1 by key, DB cascade on delete). Four joined subclass tables hang off it:

| Type | Tables and columns |
|---|---|
| flight | `flight_details(origin, destination, fare)`; `flight_coupons(flight_number, carrier, from, to, departure, arrival)`, one row per coupon, ordered |
| hotel | `hotel_details(name, street, city, country, check_in, check_out, nights, room_rate_per_night, extras_json)` |
| rail | `rail_details` (thin, required by joined mapping); `rail_legs(origin, destination, departure, arrival, train_number, service_class)`, one row per leg, ordered |
| navan_fee | `navan_fee_details(fee_kind, description, related_external_id)` with `CHECK (fee_kind IN ('trip_fee','agent_call_fee'))` |

Why typed tables and not one JSON column: required fields get `NOT NULL`, `fee_kind` gets a real `CHECK`, and coupons and legs stay queryable ("all invoices on BA982"). Hotel `extras` is optional, never queried and not part of any total, so it is stored as opaque JSON. Why composition and not inheritance on `transactions`: `transactions` stays a plain table, so row locks, duplicate lookups and lists never join subclass tables. `transactions.type` is a copy of the metadata subtype, set by the service and immutable.

Validation is Bean Validation on the entities, run before persist and before commit on PATCH. Jackson maps the `metadata` JSON directly onto the metadata entities; this coupling is accepted because the JSON shape and the tables are one-to-one. Metadata keys outside the required ones are not persisted (except hotel `extras`).

## 3. Invoice snapshot

- Creating an invoice **copies** each transaction into `invoice_lines` (`type`, `external_id`, `currency`, `occurred_at`, `total`, `metadata_json`) and its tax and fee lines into `invoice_line_tax_lines` / `invoice_line_fee_lines`. `GET /invoices/{id}` reads **only** these snapshot tables.
- `invoice_lines.transaction_id` is a real foreign key (`ON DELETE RESTRICT`).
- Rollups are computed from the frozen rows: tax grouped by `(name, rate)`, fees by `name`. Navan fee transactions count toward totals but not toward the fee rollup. Totals are the sum of lines. Mixed currency and a repeated id are **409**.
- **Frozen:** PATCH or DELETE of a transaction that is on any invoice returns 409 and writes nothing. The check runs after a row lock (`SELECT ... FOR UPDATE` through JPA `PESSIMISTIC_WRITE`); invoice creation takes the same lock, so the two serialize and an invoice can never snapshot a half-edited transaction. The FK `RESTRICT` backs up delete.
- `PATCH /invoices/{id}` rebuilds the snapshot from current transactions in one transaction (old lines bulk-deleted, then new lines inserted, so a retained transaction cannot hit the unique key). `DELETE /invoices/{id}` removes only the invoice and its snapshot rows.
- Transaction rows are locked in ascending id order to avoid deadlocks between overlapping invoice writes.

## 4. High volume

What fails first, in order, and what handles it:

1. **Duplicate POST races:** the unique constraint (section 1).
2. **PATCH vs invoice create on a hot row:** the row lock; fixed lock order; `LOCK_TIMEOUT` is 10s and a timeout maps to 503.
3. **Large lists:** `limit` defaults to 50, max 100 (above gives 400). List queries select header columns only, use `ORDER BY id` with `fetch first n rows` (limit + 1 rows to compute `hasMore`, no `count(*)`), and never load entities or paginate in memory.
4. **Lookups:** every hot path is an index seek.

Indexes: `uq_txn_external_id` (duplicate check); primary keys on every table (`GET` by id, locks, lists); `ix_invline_txn` on `invoice_lines(transaction_id)` ("is this transaction invoiced?" and the RESTRICT check); `uq_invline_inv_txn (invoice_id, transaction_id)` (also serves invoice-line lookups); FK indexes on the tax and fee tables.

Limits: offset pagination degrades at very large offsets (next step: keyset paging `WHERE id > ?`); a single DB node; H2 file mode for the exercise.