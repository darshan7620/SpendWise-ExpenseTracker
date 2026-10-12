package com.spendwise;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:phasedb;NON_KEYWORDS=MONTH,YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=phase-five-test-secret-key-1234567890abcdef"
})
class DashboardReportingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String registerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private long createCategory(String token, String name, String type) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"" + name + "\",\"type\":\"" + type + "\",\"description\":\"test\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.categoryId")).longValue();
    }

    private void createTransaction(String token, String title, String amount, String type,
                                   String date, Long categoryId) throws Exception {
        String body = "{\"title\":\"" + title + "\",\"amount\":" + amount + ",\"type\":\"" + type
                + "\",\"transactionDate\":\"" + date + "\",\"paymentMethod\":\"CASH\""
                + (categoryId != null ? ",\"categoryId\":" + categoryId : "") + "}";
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private MvcResult performGet(String token, String url) throws Exception {
        return mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
    }

    private Number value(MvcResult result, String path) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), path);
    }

    private String text(MvcResult result, String path) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), path);
    }

    @Test
    void summaryCalculatesTotalsAndRespectsMonthBoundaries() throws Exception {
        String token = registerAndGetToken("summary-a@example.com");
        createTransaction(token, "Jan income", "5000.00", "INCOME", "2026-01-01", null);
        createTransaction(token, "Jan expense 1", "200.00", "EXPENSE", "2026-01-15", null);
        createTransaction(token, "Jan expense 2", "300.00", "EXPENSE", "2026-01-31", null);
        createTransaction(token, "Feb income", "1000.00", "INCOME", "2026-02-01", null);
        createTransaction(token, "Dec expense", "999.00", "EXPENSE", "2025-12-31", null);

        MvcResult january = performGet(token, "/api/v1/dashboard/summary?year=2026&month=1");
        assertEquals(5000.0, value(january, "$.totalIncome").doubleValue(), 0.001);
        assertEquals(500.0, value(january, "$.totalExpenses").doubleValue(), 0.001);
        assertEquals(4500.0, value(january, "$.balance").doubleValue(), 0.001);
        assertEquals(3, value(january, "$.transactionCount").intValue());
        assertEquals(1, value(january, "$.incomeCount").intValue());
        assertEquals(2, value(january, "$.expenseCount").intValue());

        MvcResult year = performGet(token, "/api/v1/dashboard/summary?year=2026");
        assertEquals(6000.0, value(year, "$.totalIncome").doubleValue(), 0.001);
        assertEquals(500.0, value(year, "$.totalExpenses").doubleValue(), 0.001);
        assertEquals(4, value(year, "$.transactionCount").intValue());
    }

    @Test
    void summaryForEmptyDatasetReturnsZeros() throws Exception {
        String token = registerAndGetToken("summary-empty@example.com");
        MvcResult result = performGet(token, "/api/v1/dashboard/summary?year=2026&month=5");
        assertEquals(0.0, value(result, "$.totalIncome").doubleValue(), 0.001);
        assertEquals(0.0, value(result, "$.totalExpenses").doubleValue(), 0.001);
        assertEquals(0.0, value(result, "$.balance").doubleValue(), 0.001);
        assertEquals(0, value(result, "$.transactionCount").intValue());
    }

    @Test
    void monthlyTrendsGroupsByMonthAndFillsGaps() throws Exception {
        String token = registerAndGetToken("trends@example.com");
        YearMonth current = YearMonth.now();
        YearMonth twoAgo = current.minusMonths(2);
        createTransaction(token, "Income now", "1000.00", "INCOME", current.atDay(1).toString(), null);
        createTransaction(token, "Expense two ago", "400.00", "EXPENSE", twoAgo.atDay(1).toString(), null);

        MvcResult result = performGet(token, "/api/v1/dashboard/monthly-trends?months=3");
        assertEquals(3, value(result, "$.length()").intValue());
        assertEquals(twoAgo.getMonthValue(), value(result, "$[0].month").intValue());
        assertEquals(0.0, value(result, "$[0].totalIncome").doubleValue(), 0.001);
        assertEquals(400.0, value(result, "$[0].totalExpenses").doubleValue(), 0.001);
        assertEquals(-400.0, value(result, "$[0].balance").doubleValue(), 0.001);
        assertEquals(0.0, value(result, "$[1].totalExpenses").doubleValue(), 0.001);
        assertEquals(current.getMonthValue(), value(result, "$[2].month").intValue());
        assertEquals(1000.0, value(result, "$[2].totalIncome").doubleValue(), 0.001);
    }

    @Test
    void categoryBreakdownAggregatesAndComputesPercentages() throws Exception {
        String token = registerAndGetToken("breakdown@example.com");
        long food = createCategory(token, "Food", "EXPENSE");
        long transport = createCategory(token, "Transport", "EXPENSE");
        long salary = createCategory(token, "Salary", "INCOME");
        createTransaction(token, "Lunch", "200.00", "EXPENSE", "2026-01-10", food);
        createTransaction(token, "Bus", "300.00", "EXPENSE", "2026-01-20", transport);
        createTransaction(token, "Pay", "5000.00", "INCOME", "2026-01-05", salary);

        MvcResult result = performGet(token, "/api/v1/dashboard/category-breakdown?type=EXPENSE&year=2026&month=1");
        assertEquals(500.0, value(result, "$.totalAmount").doubleValue(), 0.001);
        assertEquals(2, value(result, "$.categories.length()").intValue());
        assertEquals("Transport", text(result, "$.categories[0].categoryName"));
        assertEquals(300.0, value(result, "$.categories[0].totalAmount").doubleValue(), 0.001);
        assertEquals(60.0, value(result, "$.categories[0].percentage").doubleValue(), 0.001);
        assertEquals("Food", text(result, "$.categories[1].categoryName"));
        assertEquals(40.0, value(result, "$.categories[1].percentage").doubleValue(), 0.001);
    }

    @Test
    void monthlyReportReturnsPeriodTotalsAndBreakdowns() throws Exception {
        String token = registerAndGetToken("report-monthly@example.com");
        long food = createCategory(token, "Food", "EXPENSE");
        long transport = createCategory(token, "Transport", "EXPENSE");
        long salary = createCategory(token, "Salary", "INCOME");
        createTransaction(token, "Jan pay", "5000.00", "INCOME", "2026-01-15", salary);
        createTransaction(token, "Jan food", "200.00", "EXPENSE", "2026-01-20", food);
        createTransaction(token, "Jan transport", "300.00", "EXPENSE", "2026-01-25", transport);
        createTransaction(token, "Feb transport", "300.00", "EXPENSE", "2026-02-10", transport);
        createTransaction(token, "Mar pay", "1000.00", "INCOME", "2026-03-05", salary);

        MvcResult result = performGet(token, "/api/v1/reports/monthly?year=2026&month=1");
        assertEquals(5000.0, value(result, "$.totalIncome").doubleValue(), 0.001);
        assertEquals(500.0, value(result, "$.totalExpenses").doubleValue(), 0.001);
        assertEquals(4500.0, value(result, "$.balance").doubleValue(), 0.001);
        assertEquals(3, value(result, "$.transactionCount").intValue());
        assertEquals(2, value(result, "$.expenseByCategory.length()").intValue());
        assertEquals(1, value(result, "$.incomeByCategory.length()").intValue());
        assertEquals("Salary", text(result, "$.incomeByCategory[0].categoryName"));
        assertEquals(100.0, value(result, "$.incomeByCategory[0].percentage").doubleValue(), 0.001);
    }

    @Test
    void yearlyReportAggregatesTwelveMonths() throws Exception {
        String token = registerAndGetToken("report-yearly@example.com");
        long food = createCategory(token, "Food", "EXPENSE");
        long transport = createCategory(token, "Transport", "EXPENSE");
        long salary = createCategory(token, "Salary", "INCOME");
        createTransaction(token, "Jan pay", "5000.00", "INCOME", "2026-01-15", salary);
        createTransaction(token, "Jan food", "200.00", "EXPENSE", "2026-01-20", food);
        createTransaction(token, "Jan transport", "300.00", "EXPENSE", "2026-01-25", transport);
        createTransaction(token, "Feb transport", "300.00", "EXPENSE", "2026-02-10", transport);
        createTransaction(token, "Mar pay", "1000.00", "INCOME", "2026-03-05", salary);
        createTransaction(token, "Prior year", "999.00", "EXPENSE", "2025-12-31", food);

        MvcResult result = performGet(token, "/api/v1/reports/yearly?year=2026");
        assertEquals(6000.0, value(result, "$.totalIncome").doubleValue(), 0.001);
        assertEquals(800.0, value(result, "$.totalExpenses").doubleValue(), 0.001);
        assertEquals(5200.0, value(result, "$.balance").doubleValue(), 0.001);
        assertEquals(5, value(result, "$.transactionCount").intValue());
        assertEquals(12, value(result, "$.monthlyBreakdown.length()").intValue());
        assertEquals(5000.0, value(result, "$.monthlyBreakdown[0].totalIncome").doubleValue(), 0.001);
        assertEquals(500.0, value(result, "$.monthlyBreakdown[0].totalExpenses").doubleValue(), 0.001);
        assertEquals(300.0, value(result, "$.monthlyBreakdown[1].totalExpenses").doubleValue(), 0.001);
        assertEquals(1000.0, value(result, "$.monthlyBreakdown[2].totalIncome").doubleValue(), 0.001);
        assertEquals("Transport", text(result, "$.expenseByCategory[0].categoryName"));
        assertEquals(600.0, value(result, "$.expenseByCategory[0].totalAmount").doubleValue(), 0.001);
        assertEquals("Salary", text(result, "$.incomeByCategory[0].categoryName"));
        assertEquals(6000.0, value(result, "$.incomeByCategory[0].totalAmount").doubleValue(), 0.001);
    }

    @Test
    void dashboardDataIsIsolatedPerUser() throws Exception {
        String tokenG = registerAndGetToken("isolation-g@example.com");
        createTransaction(tokenG, "G income", "100.00", "INCOME", "2026-01-05", null);

        String tokenH = registerAndGetToken("isolation-h@example.com");
        createTransaction(tokenH, "H income", "700.00", "INCOME", "2026-01-05", null);
        createTransaction(tokenH, "H expense", "700.00", "EXPENSE", "2026-01-06", null);

        MvcResult first = performGet(tokenG, "/api/v1/dashboard/summary?year=2026&month=1");
        assertEquals(100.0, value(first, "$.totalIncome").doubleValue(), 0.001);
        assertEquals(0.0, value(first, "$.totalExpenses").doubleValue(), 0.001);
        assertEquals(1, value(first, "$.transactionCount").intValue());

        MvcResult second = performGet(tokenH, "/api/v1/dashboard/summary?year=2026&month=1");
        assertEquals(700.0, value(second, "$.totalIncome").doubleValue(), 0.001);
        assertEquals(700.0, value(second, "$.totalExpenses").doubleValue(), 0.001);
        assertEquals(0.0, value(second, "$.balance").doubleValue(), 0.001);
        assertEquals(2, value(second, "$.transactionCount").intValue());
    }

    @Test
    void invalidParametersReturnBadRequest() throws Exception {
        String token = registerAndGetToken("validation@example.com");
        mockMvc.perform(get("/api/v1/dashboard/summary").param("month", "13")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/reports/monthly").param("year", "1800")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/dashboard/monthly-trends").param("months", "0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/dashboard/summary").param("year", "abc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }
}
