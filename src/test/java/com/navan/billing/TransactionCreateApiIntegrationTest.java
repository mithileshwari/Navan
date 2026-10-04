package com.navan.billing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.navan.billing.domain.Ids;
import com.navan.billing.entity.TransactionEntity;
import com.navan.billing.repo.InvoiceLineRepository;
import com.navan.billing.repo.InvoiceRepository;
import com.navan.billing.repo.TransactionRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=8081",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.sql.init.mode=always"
        })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionCreateApiIntegrationTest {
    private static final String TEST_DATABASE_URL =
            "jdbc:h2:file:./target/task-b-create-types;MODE=PostgreSQL;LOCK_TIMEOUT=10000;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    private static final List<String> SEED_NAMES =
            List.of("flight", "hotel", "rail", "trip_fee", "agent_call_fee");
    private final Map<String, String> transactionIdsBySeed = new LinkedHashMap<>();
    private String tripInvoiceId;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> TEST_DATABASE_URL);
    }

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvoiceLineRepository invoiceLines;

    @Autowired
    private InvoiceRepository invoices;

    @Autowired
    private TransactionRepository transactions;

    @BeforeAll
    void createSharedFixtureTransactions() throws IOException {
        invoiceLines.deleteAll();
        invoices.deleteAll();
        transactions.deleteAll();

        JsonNode seeds = objectMapper.readTree(
                new ClassPathResource("fixtures/task-b/seed-payloads.json").getInputStream());
        transactionIdsBySeed.putAll(createFixtureTransactions(seeds));
    }

    @Test
    @Order(1)
    @DisplayName("💱 Mixed currencies → POST EUR + USD invoice, inspect response")
    void postsMixedCurrencyInvoiceAndPrintsResponses() throws IOException {
        System.out.println("\n💱 Mixed currencies → POST EUR + USD invoice, inspect response");
        JsonNode seeds = objectMapper.readTree(
                new ClassPathResource("fixtures/task-b/seed-payloads.json").getInputStream());
        ObjectNode usdRequest = ((ObjectNode) seeds.path("flight").deepCopy())
                .put("external_id", "flight-TXL-LHR-2026-03-11-USD")
                .put("currency", "USD");

        ResponseEntity<String> usdCreateResponse = postAndPrint(
                "POST /transactions (USD flight)", "/transactions", usdRequest);
        JsonNode usdBody = usdCreateResponse.getBody() == null
                ? null : objectMapper.readTree(usdCreateResponse.getBody());
        String usdTransactionId = usdBody == null ? "" : usdBody.path("id").asText();

        ArrayNode transactionIds = objectMapper.createArrayNode()
                .add(transactionIdsBySeed.get("flight"))
                .add(usdTransactionId);
        ObjectNode invoiceRequest = objectMapper.createObjectNode();
        invoiceRequest.set("transaction_ids", transactionIds);
        postAndPrint("POST /invoices (EUR + USD)", "/invoices", invoiceRequest);
    }

    @Test
    @Order(2)
    @DisplayName("🧾 Fixture transactions → create trip invoice → inspect API responses")
    void postsFixtureExamplesAndPrintsApiResponses() throws IOException {
        tripInvoiceId = createTripInvoice(transactionIdsBySeed);
        printInvoiceGet(tripInvoiceId);
    }

    @Test
    @Order(3)
    @DisplayName("🛡️ Invoiced transaction → PATCH flight, inspect response and invoice snapshot")
    void patchesInvoicedFlightAndPrintsResponses() throws IOException {
        System.out.println("\n🛡️ Invoiced transaction → PATCH flight, inspect response and invoice snapshot");
        if (tripInvoiceId == null || tripInvoiceId.isBlank()) {
            tripInvoiceId = createTripInvoice(transactionIdsBySeed);
        }

        String flightId = transactionIdsBySeed.get("flight");
        ObjectNode patchRequest = objectMapper.createObjectNode().put("total", new BigDecimal("999.00"));
        System.out.printf("%nPATCH /transactions/%s request:%n", flightId);
        printJson(patchRequest);
        ResponseEntity<String> patchResponse = http.exchange(
                "/transactions/" + flightId, HttpMethod.PATCH, new HttpEntity<>(patchRequest), String.class);
        System.out.printf("PATCH /transactions/%s response -> %s%n", flightId, patchResponse.getStatusCode());
        printJson(patchResponse.getBody());

        printInvoiceGet(tripInvoiceId);
    }

    @Test
    @Order(4)
    @DisplayName("🗑️ Invoiced transaction → DELETE flight, inspect response and GET")
    void deletesInvoicedFlightAndPrintsResponses() throws IOException {
        System.out.println("\n🗑️ Invoiced transaction → DELETE flight, inspect response and GET");
        if (tripInvoiceId == null || tripInvoiceId.isBlank()) {
            tripInvoiceId = createTripInvoice(transactionIdsBySeed);
        }

        String flightId = transactionIdsBySeed.get("flight");
        System.out.printf("%nDELETE /transactions/%s request: <no body>%n", flightId);
        ResponseEntity<String> deleteResponse = http.exchange(
                "/transactions/" + flightId, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        System.out.printf("DELETE /transactions/%s response -> %s%n", flightId, deleteResponse.getStatusCode());
        printJson(deleteResponse.getBody());

        ResponseEntity<String> getResponse = http.getForEntity("/transactions/" + flightId, String.class);
        System.out.printf("GET /transactions/%s after DELETE response -> %s%n",
                flightId, getResponse.getStatusCode());
        printJson(getResponse.getBody());
    }

    @Test
    @Order(5)
    @DisplayName("📸 Invoice snapshot → change stored flight total, inspect invoice GET")
    void changesStoredFlightTotalAndPrintsInvoiceSnapshot() throws IOException {
        System.out.println("\n📸 Invoice snapshot → change stored flight total, inspect invoice GET");
        if (tripInvoiceId == null || tripInvoiceId.isBlank()) {
            tripInvoiceId = createTripInvoice(transactionIdsBySeed);
        }

        String flightId = transactionIdsBySeed.get("flight");
        ResponseEntity<String> flightResponse = http.getForEntity("/transactions/" + flightId, String.class);
        System.out.printf("GET /transactions/%s before stored-total change -> %s%n",
                flightId, flightResponse.getStatusCode());
        printJson(flightResponse.getBody());

        long flightDatabaseId = Ids.parseTxn(flightId);
        TransactionEntity flight = transactions.findById(flightDatabaseId).orElseThrow();
        flight.setTotal(new BigDecimal("999.00"));
        transactions.save(flight);

        ResponseEntity<String> changedFlightResponse = http.getForEntity(
                "/transactions/" + flightId, String.class);
        System.out.printf("GET /transactions/%s after stored-total change -> %s%n",
                flightId, changedFlightResponse.getStatusCode());
        printJson(changedFlightResponse.getBody());

        printInvoiceGet(tripInvoiceId);
    }

    @Test
    @Order(6)
    @DisplayName("🧹 DELETE invoice → inspect invoice and transaction GETs")
    void deletesTripInvoiceAndPrintsRemainingTransactions() throws IOException {
        System.out.println("\n🧹 DELETE invoice → inspect invoice and transaction GETs");
        if (tripInvoiceId == null || tripInvoiceId.isBlank()) {
            tripInvoiceId = createTripInvoice(transactionIdsBySeed);
        }

        System.out.printf("%nDELETE /invoices/%s request: <no body>%n", tripInvoiceId);
        ResponseEntity<String> deleteResponse = http.exchange(
                "/invoices/" + tripInvoiceId, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        System.out.printf("DELETE /invoices/%s response -> %s%n", tripInvoiceId, deleteResponse.getStatusCode());
        printJson(deleteResponse.getBody());

        ResponseEntity<String> invoiceResponse = http.getForEntity("/invoices/" + tripInvoiceId, String.class);
        System.out.printf("GET /invoices/%s after DELETE response -> %s%n",
                tripInvoiceId, invoiceResponse.getStatusCode());
        printJson(invoiceResponse.getBody());

        String flightId = transactionIdsBySeed.get("flight");
        ResponseEntity<String> transactionResponse = http.getForEntity(
                "/transactions/" + flightId, String.class);
        System.out.printf("GET /transactions/%s after invoice DELETE -> %s%n",
                flightId, transactionResponse.getStatusCode());
        printJson(transactionResponse.getBody());
    }

    private Map<String, String> createFixtureTransactions(JsonNode seeds) throws IOException {
        Map<String, String> transactionIdsBySeed = new LinkedHashMap<>();
        for (String seedName : SEED_NAMES) {
            JsonNode request = seeds.path(seedName);
            System.out.printf("%nPOST /transactions (%s) request:%n", seedName);
            printJson(request);
            ResponseEntity<String> createResponse = http.postForEntity(
                    "/transactions", request, String.class);

            System.out.printf("POST /transactions (%s) response -> %s%n", seedName, createResponse.getStatusCode());
            printJson(createResponse.getBody());

            JsonNode createBody = createResponse.getBody() == null
                    ? null : objectMapper.readTree(createResponse.getBody());
            String transactionId = createBody == null ? "" : createBody.path("id").asText();
            transactionIdsBySeed.put(seedName, transactionId);
            if (!transactionId.isBlank()) {
                ResponseEntity<String> getResponse = http.getForEntity(
                        "/transactions/" + transactionId, String.class);
                System.out.printf("GET /transactions/%s response -> %s%n", transactionId, getResponse.getStatusCode());
                printJson(getResponse.getBody());
            }
        }
        return transactionIdsBySeed;
    }

    @Test
    @Order(7)
    @DisplayName("🧪 Duplicate external_id → POST twice, inspect both responses")
    void postsDuplicateExternalIdAndPrintsResponses() throws IOException {
        System.out.println("\n🧪 Duplicate external_id → POST twice, inspect both responses");
        JsonNode seeds = objectMapper.readTree(
                new ClassPathResource("fixtures/task-b/seed-payloads.json").getInputStream());

        System.out.printf("Using existing flight transaction id: %s%n", transactionIdsBySeed.get("flight"));
        printPost("duplicate_flight POST", seeds.path("duplicate_flight"));
    }

    @Test
    @Order(8)
    @DisplayName("📄 Transactions pagination → inspect default and limit=1000")
    void printsTransactionPaginationResponses() throws IOException {
        System.out.println("\n📄 Transactions pagination → inspect default and limit=1000");
        getAndPrint("GET /transactions (default limit)", "/transactions");
        getAndPrint("GET /transactions?limit=1000", "/transactions?limit=1000");
    }

    private String createTripInvoice(Map<String, String> transactionIdsBySeed) throws IOException {
        ArrayNode transactionIds = objectMapper.createArrayNode();
        for (String seedName : SEED_NAMES) {
            transactionIds.add(transactionIdsBySeed.get(seedName));
        }
        ObjectNode invoiceRequest = objectMapper.createObjectNode();
        invoiceRequest.set("transaction_ids", transactionIds);

        ResponseEntity<String> invoiceResponse = postAndPrint(
                "POST /invoices (trip)", "/invoices", invoiceRequest);
        JsonNode invoiceBody = invoiceResponse.getBody() == null
                ? null : objectMapper.readTree(invoiceResponse.getBody());
        return invoiceBody == null ? "" : invoiceBody.path("id").asText();
    }

    private void printInvoiceGet(String invoiceId) throws IOException {
        if (!invoiceId.isBlank()) {
            ResponseEntity<String> getResponse = http.getForEntity("/invoices/" + invoiceId, String.class);
            System.out.printf("GET /invoices/%s response -> %s%n", invoiceId, getResponse.getStatusCode());
            printJson(getResponse.getBody());
        }
    }

    private void printPost(String label, JsonNode request) throws IOException {
        postAndPrint(label, "/transactions", request);
    }

    private void getAndPrint(String label, String path) throws IOException {
        System.out.printf("%n%s request: <no body>%n", label);
        ResponseEntity<String> response = http.getForEntity(path, String.class);
        System.out.printf("%s response -> %s%n", label, response.getStatusCode());
        printJson(response.getBody());
    }

    private ResponseEntity<String> postAndPrint(String label, String path, JsonNode request) throws IOException {
        System.out.printf("%n%s request:%n", label);
        printJson(request);
        ResponseEntity<String> response = http.postForEntity(path, request, String.class);
        System.out.printf("%s response -> %s%n", label, response.getStatusCode());
        printJson(response.getBody());
        return response;
    }

    private void printJson(JsonNode json) throws IOException {
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
    }

    private void printJson(String json) throws IOException {
        if (json == null) {
            System.out.println("<empty response body>");
            return;
        }
        printJson(objectMapper.readTree(json));
    }
}
