package com.spendwise;

import com.jayway.jsonpath.JsonPath;
import com.spendwise.entity.Category;
import com.spendwise.entity.GoalStatus;
import com.spendwise.entity.SavingsGoal;
import com.spendwise.entity.TransactionType;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.SavingsGoalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:p7db;NON_KEYWORDS=MONTH,YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=phase-seven-test-secret-key-1234567890abcdef"
})
class ResourceCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    private String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID() + "@example.com";
    }

    private String register(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"CRUD User\",\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private long createCategory(String token, String name, String type) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"" + name + "\",\"type\":\"" + type + "\",\"description\":\"d\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.categoryId")).longValue();
    }

    private long createTransaction(String token, String title, String amount, String type, String date, Long categoryId) throws Exception {
        String body = "{\"title\":\"" + title + "\",\"amount\":" + amount + ",\"type\":\"" + type
                + "\",\"transactionDate\":\"" + date + "\",\"paymentMethod\":\"CARD\""
                + (categoryId != null ? ",\"categoryId\":" + categoryId : "") + "}";
        MvcResult result = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.transactionId")).longValue();
    }

    private long createBudget(String token, long categoryId, String limit, int month, int year) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"limitAmount\":" + limit + ",\"month\":" + month + ",\"year\":" + year + ",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.budgetId")).longValue();
    }

    private long createGoal(String token, String name, String target, String current, String date) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goalName\":\"" + name + "\",\"targetAmount\":" + target + ",\"currentAmount\":" + current + ",\"targetDate\":\"" + date + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.goalId")).longValue();
    }

    // ---------------- Transactions ----------------

    @Test
    void transactionLifecycleCreateListFilterUpdateDelete() throws Exception {
        String token = register(uniqueEmail("tx"));
        long food = createCategory(token, "Food", "EXPENSE");
        long salary = createCategory(token, "Salary", "INCOME");
        createTransaction(token, "Lunch", "200.00", "EXPENSE", "2026-01-10", food);
        createTransaction(token, "Bus", "50.00", "EXPENSE", "2026-01-11", null);
        createTransaction(token, "Pay", "5000.00", "INCOME", "2026-01-05", salary);
        createTransaction(token, "Old", "10.00", "EXPENSE", "2025-12-31", null);

        mockMvc.perform(get("/api/v1/transactions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.totalElements").value(4));

        mockMvc.perform(get("/api/v1/transactions").param("type", "EXPENSE").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/v1/transactions").param("categoryId", String.valueOf(food)).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Lunch"));

        mockMvc.perform(get("/api/v1/transactions").param("startDate", "2026-01-01").param("endDate", "2026-01-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/v1/transactions").param("title", "lun").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/v1/transactions").param("startDate", "2026-02-01").param("endDate", "2026-01-01")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        long id = createTransaction(token, "Gym", "75.00", "EXPENSE", "2026-01-20", null);
        mockMvc.perform(put("/api/v1/transactions/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Gym (updated)\",\"amount\":80.00,\"type\":\"EXPENSE\",\"transactionDate\":\"2026-01-20\",\"paymentMethod\":\"UPI\",\"categoryId\":" + food + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Gym (updated)"))
                .andExpect(jsonPath("$.categoryId").value(food));

        mockMvc.perform(get("/api/v1/transactions/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(80.0));

        mockMvc.perform(delete("/api/v1/transactions/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/transactions/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void transactionWithMismatchedCategoryTypeRejected() throws Exception {
        String token = register(uniqueEmail("tx-mismatch"));
        long incomeCategory = createCategory(token, "IncomeCat", "INCOME");
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bad\",\"amount\":10,\"type\":\"EXPENSE\",\"transactionDate\":\"2026-01-01\",\"paymentMethod\":\"CASH\",\"categoryId\":" + incomeCategory + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transactionWithUnknownCategoryReturnsNotFound() throws Exception {
        String token = register(uniqueEmail("tx-badcat"));
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bad\",\"amount\":10,\"type\":\"EXPENSE\",\"transactionDate\":\"2026-01-01\",\"paymentMethod\":\"CASH\",\"categoryId\":987654}"))
                .andExpect(status().isNotFound());
    }

    // ---------------- Categories ----------------

    @Test
    void categoryLifecycleAndDuplicateRejected() throws Exception {
        String token = register(uniqueEmail("cat"));
        long id = createCategory(token, "Groceries", "EXPENSE");

        mockMvc.perform(get("/api/v1/categories/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPredefined").value(false))
                .andExpect(jsonPath("$.ownerId").isNotEmpty());

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"Groceries\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/categories/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"Groceries & Home\",\"type\":\"EXPENSE\",\"description\":\"upd\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Groceries & Home"));

        mockMvc.perform(delete("/api/v1/categories/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/categories/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void categoryReferencedByTransactionCannotBeDeleted() throws Exception {
        String token = register(uniqueEmail("cat-inuse"));
        long id = createCategory(token, "InUse", "EXPENSE");
        createTransaction(token, "Uses category", "10.00", "EXPENSE", "2026-01-01", id);
        mockMvc.perform(delete("/api/v1/categories/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void predefinedCategoryIsVisibleButImmutable() throws Exception {
        String token = register(uniqueEmail("cat-predef"));
        Category predefined = new Category();
        predefined.setCategoryName("Shared-" + UUID.randomUUID());
        predefined.setType(TransactionType.EXPENSE);
        predefined.setDescription("system");
        predefined.setOwner(null);
        Category saved = categoryRepository.save(predefined);

        mockMvc.perform(get("/api/v1/categories/" + saved.getCategoryId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPredefined").value(true))
                .andExpect(jsonPath("$.ownerId").doesNotExist());

        mockMvc.perform(put("/api/v1/categories/" + saved.getCategoryId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"Hacked\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/v1/categories/" + saved.getCategoryId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ---------------- Budgets ----------------

    @Test
    void budgetLifecycleWithServerCalculatedIndicators() throws Exception {
        String token = register(uniqueEmail("budget"));
        long rent = createCategory(token, "Rent", "EXPENSE");
        createTransaction(token, "Rent part 1", "500.00", "EXPENSE", "2026-03-05", rent);
        createTransaction(token, "Rent part 2", "350.00", "EXPENSE", "2026-03-06", rent);
        createTransaction(token, "Ignored income", "9999.00", "INCOME", "2026-03-07", null);
        createTransaction(token, "Other month", "999.00", "EXPENSE", "2026-04-01", rent);

        long budgetId = createBudget(token, rent, "1000.00", 3, 2026);

        mockMvc.perform(get("/api/v1/budgets/" + budgetId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentAmount").value(850.0))
                .andExpect(jsonPath("$.remainingAmount").value(150.0))
                .andExpect(jsonPath("$.percentageUsed").value(85.0))
                .andExpect(jsonPath("$.warningStatus").value("WARNING"));

        createTransaction(token, "Rent part 3", "200.00", "EXPENSE", "2026-03-20", rent);

        mockMvc.perform(get("/api/v1/budgets/" + budgetId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentAmount").value(1050.0))
                .andExpect(jsonPath("$.remainingAmount").value(0.0))
                .andExpect(jsonPath("$.percentageUsed").value(105.0))
                .andExpect(jsonPath("$.warningStatus").value("EXCEEDED"));

        mockMvc.perform(post("/api/v1/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"limitAmount\":100,\"month\":3,\"year\":2026,\"categoryId\":" + rent + "}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/budgets/" + budgetId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"limitAmount\":2000.00,\"month\":3,\"year\":2026,\"categoryId\":" + rent + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limitAmount").value(2000.0))
                .andExpect(jsonPath("$.warningStatus").value("NORMAL"));

        mockMvc.perform(delete("/api/v1/budgets/" + budgetId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/budgets/" + budgetId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ---------------- Savings goals ----------------

    @Test
    void savingsGoalLifecycleAndProgress() throws Exception {
        String token = register(uniqueEmail("goal"));
        long id = createGoal(token, "Emergency", "1000.00", "200.00", "2026-12-31");

        mockMvc.perform(get("/api/v1/goals/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPercentage").value(20.0))
                .andExpect(jsonPath("$.remainingAmount").value(800.0))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/goals/" + id + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountToAdd\":300.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(500.0))
                .andExpect(jsonPath("$.progressPercentage").value(50.0))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/goals/" + id + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountToAdd\":900.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(1000.0))
                .andExpect(jsonPath("$.progressPercentage").value(100.0))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(delete("/api/v1/goals/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void progressOnCancelledGoalRejected() throws Exception {
        String token = register(uniqueEmail("goal-cancel"));
        long id = createGoal(token, "Vacation", "5000.00", "0.00", "2026-12-31");
        SavingsGoal goal = savingsGoalRepository.findById(id).orElseThrow();
        goal.setStatus(GoalStatus.CANCELLED);
        savingsGoalRepository.save(goal);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/goals/" + id + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountToAdd\":100.00}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void savingsGoalValidationRejectsNonPositiveTarget() throws Exception {
        String token = register(uniqueEmail("goal-invalid"));
        mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goalName\":\"Bad\",\"targetAmount\":0,\"currentAmount\":0,\"targetDate\":\"2026-12-31\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- Cross-user isolation ----------------

    @Test
    void resourcesAreIsolatedBetweenUsers() throws Exception {
        String tokenA = register(uniqueEmail("iso-a"));
        String tokenB = register(uniqueEmail("iso-b"));

        long catA = createCategory(tokenA, "A-Food", "EXPENSE");
        long txA = createTransaction(tokenA, "A-Lunch", "100.00", "EXPENSE", "2026-02-01", catA);
        long budgetA = createBudget(tokenA, catA, "500.00", 2, 2026);
        long goalA = createGoal(tokenA, "A-Goal", "1000.00", "100.00", "2026-12-31");

        mockMvc.perform(get("/api/v1/categories").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.categoryId == " + catA + ")]").isEmpty());

        mockMvc.perform(get("/api/v1/transactions").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/v1/transactions/" + txA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/categories/" + catA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/budgets/" + budgetA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/goals/" + goalA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/transactions/" + txA)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hacked\",\"amount\":1,\"type\":\"EXPENSE\",\"transactionDate\":\"2026-02-01\",\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/budgets/" + budgetA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/goals/" + goalA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        // Owner still sees their data intact.
        mockMvc.perform(get("/api/v1/transactions/" + txA).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("A-Lunch"))
                .andExpect(jsonPath("$.amount").value(100.0));
    }
}
