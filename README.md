# Navan billing API

## Run the API and tests

From the repository root, start the API with:

```bash
./mvnw spring-boot:run
```

Run the automated tests with:

```bash
./mvnw test
```

The API listens on `http://localhost:8080`. The H2 console is at `http://localhost:8080/h2-console`.

## cURL examples

Run these from the repository root. The create examples read the matching request body from the fixture file; `jq` is required for that extraction.

```bash
export BASE=http://localhost:8080
export SEEDS=src/test/resources/fixtures/task-b/seed-payloads.json
```

### Create transactions

Each command creates the corresponding transaction type. The fixture includes both Navan fee kinds.

```bash
curl -i -X POST "$BASE/transactions" -H 'Content-Type: application/json' --data "$(jq -c '.flight' "$SEEDS")"
curl -i -X POST "$BASE/transactions" -H 'Content-Type: application/json' --data "$(jq -c '.hotel' "$SEEDS")"
curl -i -X POST "$BASE/transactions" -H 'Content-Type: application/json' --data "$(jq -c '.rail' "$SEEDS")"
curl -i -X POST "$BASE/transactions" -H 'Content-Type: application/json' --data "$(jq -c '.trip_fee' "$SEEDS")"
curl -i -X POST "$BASE/transactions" -H 'Content-Type: application/json' --data "$(jq -c '.agent_call_fee' "$SEEDS")"
```

Copy the returned IDs for the following examples:

```bash
export FLIGHT_ID=txn_1
export HOTEL_ID=txn_2
export RAIL_ID=txn_3
export TRIP_FEE_ID=txn_4
export AGENT_FEE_ID=txn_5
```

### Duplicate create

This pair uses a fresh `external_id`, so it can be rerun. The first request creates the transaction; the second uses the same ID and returns `409 duplicate_external_id`.

```bash
DUPLICATE_ID="curl-duplicate-$(date +%s)"
curl -i -X POST "$BASE/transactions" -H 'Content-Type: application/json' --data "$(jq -c --arg id "$DUPLICATE_ID" '.flight | .external_id = $id' "$SEEDS")"
curl -i -X POST "$BASE/transactions" -H 'Content-Type: application/json' --data "$(jq -c --arg id "$DUPLICATE_ID" '.duplicate_flight | .external_id = $id' "$SEEDS")"
```

### Get, list, patch, and delete transactions

```bash
curl -i "$BASE/transactions/$FLIGHT_ID"
curl -i "$BASE/transactions"
curl -i "$BASE/transactions?limit=50&offset=0"
```

Patch mutable fields such as `total`, `occurred_at`, tax lines, fee lines, or metadata. Omit immutable fields (`type`, `external_id`, and `currency`).

```bash
curl -i -X PATCH "$BASE/transactions/$FLIGHT_ID" \
  -H 'Content-Type: application/json' \
  --data '{"total":221.00}'
```

After a transaction is on an invoice, PATCH and DELETE return `409 transaction_invoiced`:

```bash
curl -i -X PATCH "$BASE/transactions/$FLIGHT_ID" \
  -H 'Content-Type: application/json' \
  --data '{"total":222.00}'
curl -i -X DELETE "$BASE/transactions/$FLIGHT_ID"
```

### Create, get, list, patch, and delete invoices

Choose one example below to create an invoice. A transaction can belong to only one invoice.

```bash
curl -i -X POST "$BASE/invoices" \
  -H 'Content-Type: application/json' \
  --data "{\"transaction_id\":\"$FLIGHT_ID\"}"

curl -i -X POST "$BASE/invoices" \
  -H 'Content-Type: application/json' \
  --data "{\"transaction_ids\":[\"$FLIGHT_ID\",\"$HOTEL_ID\",\"$RAIL_ID\",\"$TRIP_FEE_ID\",\"$AGENT_FEE_ID\"]}"
```

Copy the returned invoice ID, then use it below:

```bash
export INVOICE_ID=inv_1
curl -i "$BASE/invoices/$INVOICE_ID"
curl -i "$BASE/invoices"
curl -i "$BASE/invoices?limit=50&offset=0"
```

PATCH replaces the invoice’s transaction set:

```bash
curl -i -X PATCH "$BASE/invoices/$INVOICE_ID" \
  -H 'Content-Type: application/json' \
  --data "{\"transaction_ids\":[\"$FLIGHT_ID\",\"$HOTEL_ID\"]}"
```

DELETE removes the invoice and its snapshot lines. The transactions remain available through `GET /transactions/{id}`.

```bash
curl -i -X DELETE "$BASE/invoices/$INVOICE_ID"
curl -i "$BASE/transactions/$FLIGHT_ID"
```

Transaction and invoice list endpoints default to 50 items and accept limits from 1 to 100. A request with `limit=1000` returns `400`.
