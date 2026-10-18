package com.spendwise;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Boots the full application on a real (random) HTTP port and drives the public REST API
 * over the network stack, exercising the servlet container, security filter chain and JSON
 * serialization end to end. Uses an in-memory H2 database instead of MySQL.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:p7http;NON_KEYWORDS=MONTH,YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=phase-seven-http-secret-key-1234567890abcdef"
})
class FullStackHttpIntegrationTest {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    private String base() {
        return "http://localhost:" + port;
    }

    private HttpResponse<String> call(String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(base() + path));
        if (body != null) {
            builder.header("Content-Type", "application/json");
        }
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        builder.method(method, publisher);
        return CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID() + "@example.com";
    }

    private String register(String email) throws Exception {
        HttpResponse<String> response = call("POST", "/api/v1/auth/register", null,
                "{\"fullName\":\"HTTP User\",\"email\":\"" + email + "\",\"password\":\"secret123\"}");
        assertEquals(201, response.statusCode(), response.body());
        return JsonPath.read(response.body(), "$.accessToken");
    }

    private long createCategory(String token, String name, String type) throws Exception {
        HttpResponse<String> response = call("POST", "/api/v1/categories", token,
                "{\"categoryName\":\"" + name + "\",\"type\":\"" + type + "\",\"description\":\"http\"}");
        assertEquals(201, response.statusCode(), response.body());
        return ((Number) JsonPath.read(response.body(), "$.categoryId")).longValue();
    }

    private long createTransaction(String token, String title, String amount, String type, String date, Long categoryId) throws Exception {
        String body = "{\"title\":\"" + title + "\",\"amount\":" + amount + ",\"type\":\"" + type
                + "\",\"transactionDate\":\"" + date + "\",\"paymentMethod\":\"UPI\""
                + (categoryId != null ? ",\"categoryId\":" + categoryId : "") + "}";
        HttpResponse<String> response = call("POST", "/api/v1/transactions", token, body);
        assertEquals(201, response.statusCode(), response.body());
        return ((Number) JsonPath.read(response.body(), "$.transactionId")).longValue();
    }

    @Test
    void fullUserWorkflowOverRealHttp() throws Exception {
        String token = register(uniqueEmail("http-flow"));
        long food = createCategory(token, "Food", "EXPENSE");
        long salary = createCategory(token, "Salary", "INCOME");

        createTransaction(token, "Salary January", "5000.00", "INCOME", "2026-01-05", salary);
        createTransaction(token, "Groceries January", "200.00", "EXPENSE", "2026-01-10", food);
        createTransaction(token, "Groceries January 2", "100.00", "EXPENSE", "2026-01-20", food);

        // current user
        HttpResponse<String> me = call("GET", "/api/v1/auth/me", token, null);
        assertEquals(200, me.statusCode());
        assertEquals("USER", JsonPath.read(me.body(), "$.role"));

        // transaction list + filter
        HttpResponse<String> list = call("GET", "/api/v1/transactions?type=EXPENSE", token, null);
        assertEquals(200, list.statusCode());
        assertEquals(2, ((Number) JsonPath.read(list.body(), "$.totalElements")).intValue());

        // budget with server-calculated spending, triggered by a reloaded GET
        HttpResponse<String> budgetCreate = call("POST", "/api/v1/budgets", token,
                "{\"limitAmount\":250.00,\"month\":1,\"year\":2026,\"categoryId\":" + food + "}");
        assertEquals(201, budgetCreate.statusCode());
        long budgetId = ((Number) JsonPath.read(budgetCreate.body(), "$.budgetId")).longValue();
        HttpResponse<String> budget = call("GET", "/api/v1/budgets/" + budgetId, token, null);
        assertEquals(300.0, ((Number) JsonPath.read(budget.body(), "$.spentAmount")).doubleValue(), 0.001);
        assertEquals("EXCEEDED", JsonPath.read(budget.body(), "$.warningStatus"));

        // savings goal + progress
        HttpResponse<String> goalCreate = call("POST", "/api/v1/goals", token,
                "{\"goalName\":\"Trip\",\"targetAmount\":1000.00,\"currentAmount\":0.00,\"targetDate\":\"2026-12-31\"}");
        assertEquals(201, goalCreate.statusCode());
        long goalId = ((Number) JsonPath.read(goalCreate.body(), "$.goalId")).longValue();
        HttpResponse<String> progress = call("PATCH", "/api/v1/goals/" + goalId + "/progress", token,
                "{\"amountToAdd\":400.00}");
        assertEquals(200, progress.statusCode());
        assertEquals(40.0, ((Number) JsonPath.read(progress.body(), "$.progressPercentage")).doubleValue(), 0.001);

        // dashboard summary + trends + breakdown
        HttpResponse<String> summary = call("GET", "/api/v1/dashboard/summary?year=2026&month=1", token, null);
        assertEquals(5000.0, ((Number) JsonPath.read(summary.body(), "$.totalIncome")).doubleValue(), 0.001);
        assertEquals(300.0, ((Number) JsonPath.read(summary.body(), "$.totalExpenses")).doubleValue(), 0.001);
        assertEquals(4700.0, ((Number) JsonPath.read(summary.body(), "$.balance")).doubleValue(), 0.001);

        HttpResponse<String> trends = call("GET", "/api/v1/dashboard/monthly-trends?months=3", token, null);
        assertEquals(200, trends.statusCode());
        assertEquals(3, ((Number) JsonPath.read(trends.body(), "$.length()")).intValue());

        HttpResponse<String> breakdown = call("GET", "/api/v1/dashboard/category-breakdown?type=EXPENSE&year=2026&month=1", token, null);
        assertEquals("Food", JsonPath.read(breakdown.body(), "$.categories[0].categoryName"));
        assertEquals(300.0, ((Number) JsonPath.read(breakdown.body(), "$.totalAmount")).doubleValue(), 0.001);

        // reports
        HttpResponse<String> monthly = call("GET", "/api/v1/reports/monthly?year=2026&month=1", token, null);
        assertEquals(5000.0, ((Number) JsonPath.read(monthly.body(), "$.totalIncome")).doubleValue(), 0.001);
        HttpResponse<String> yearly = call("GET", "/api/v1/reports/yearly?year=2026", token, null);
        assertEquals(12, ((Number) JsonPath.read(yearly.body(), "$.monthlyBreakdown.length()")).intValue());

        // update + delete transaction
        long tx = createTransaction(token, "Temp", "10.00", "EXPENSE", "2026-01-25", food);
        HttpResponse<String> update = call("PUT", "/api/v1/transactions/" + tx, token,
                "{\"title\":\"Temp updated\",\"amount\":15.00,\"type\":\"EXPENSE\",\"transactionDate\":\"2026-01-25\",\"paymentMethod\":\"CASH\",\"categoryId\":" + food + "}");
        assertEquals(200, update.statusCode());
        assertEquals("Temp updated", JsonPath.read(update.body(), "$.title"));
        HttpResponse<String> delete = call("DELETE", "/api/v1/transactions/" + tx, token, null);
        assertEquals(204, delete.statusCode(), delete.body());
    }

    @Test
    void crossUserIsolationOverRealHttp() throws Exception {
        String tokenA = register(uniqueEmail("http-iso-a"));
        String tokenB = register(uniqueEmail("http-iso-b"));
        long catA = createCategory(tokenA, "A Cat", "EXPENSE");
        long txA = createTransaction(tokenA, "A Tx", "42.00", "EXPENSE", "2026-02-01", catA);

        HttpResponse<String> bList = call("GET", "/api/v1/transactions", tokenB, null);
        assertEquals(0, ((Number) JsonPath.read(bList.body(), "$.totalElements")).intValue());

        HttpResponse<String> bGet = call("GET", "/api/v1/transactions/" + txA, tokenB, null);
        assertEquals(403, bGet.statusCode());
        HttpResponse<String> bDelete = call("DELETE", "/api/v1/transactions/" + txA, tokenB, null);
        assertEquals(403, bDelete.statusCode());

        HttpResponse<String> aGet = call("GET", "/api/v1/transactions/" + txA, tokenA, null);
        assertEquals(200, aGet.statusCode());
    }

    @Test
    void authAndErrorResponsesOverRealHttp() throws Exception {
        // unauthenticated
        assertEquals(401, call("GET", "/api/v1/transactions", null, null).statusCode());

        String token = register(uniqueEmail("http-errors"));

        // unknown endpoint
        assertEquals(404, call("GET", "/api/v1/nope", token, null).statusCode());

        // malformed JSON body
        assertEquals(400, call("POST", "/api/v1/transactions", token, "{oops").statusCode());

        // invalid enum in body
        assertEquals(400, call("POST", "/api/v1/transactions", token,
                "{\"title\":\"x\",\"amount\":1,\"type\":\"NOPE\",\"transactionDate\":\"2026-01-01\",\"paymentMethod\":\"CASH\"}").statusCode());

        // validation failure
        HttpResponse<String> bad = call("POST", "/api/v1/transactions", token, "{}");
        assertEquals(400, bad.statusCode());
        assertNotNull(JsonPath.read(bad.body(), "$.fieldErrors"));

        // duplicate registration
        String email = uniqueEmail("http-dup");
        register(email);
        HttpResponse<String> dup = call("POST", "/api/v1/auth/register", null,
                "{\"fullName\":\"Dup\",\"email\":\"" + email + "\",\"password\":\"secret123\"}");
        assertEquals(400, dup.statusCode());

        // invalid login
        HttpResponse<String> badLogin = call("POST", "/api/v1/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}");
        assertEquals(401, badLogin.statusCode());

        // admin forbidden for normal user
        assertEquals(403, call("GET", "/api/v1/admin/users", token, null).statusCode());
    }
}
