package io.github.alexisTrejo11.construction.company.modules.inventory;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence.InventoryItemRepository;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence.InventoryLocationRepository;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence.InventoryMovementRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PhaseNineInventoryIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private InventoryItemRepository items;
    @Autowired private InventoryLocationRepository locations;
    @Autowired private InventoryMovementRepository movements;
    @Autowired private PasswordEncoder encoder;

    private User admin;
    private String operatorEmail;

    @BeforeEach
    void setUp() {
        movements.deleteAll();
        locations.deleteAll();
        items.deleteAll();
        admin = seedUser("inventory-admin-" + System.nanoTime(), UserRole.COMPANY_ADMIN);
        operatorEmail = "inventory-operator-" + System.nanoTime() + "@example.com";
        seedUser(operatorEmail, UserRole.PROJECT_MANAGER);
    }

    @Test
    void itemEndpointsCoverCrudStatusBalanceAndEnvelope() throws Exception {
        ResultActions created = json(post("/v2/api/inventory/items"), "{\"code\":\"CEMENT\",\"name\":\"Cement\",\"category\":\"MATERIAL\",\"unit\":\"bag\",\"trackingMode\":\"QUANTITY\"}")
                .andExpect(status().isCreated());
        envelope(created);
        Long itemId = items.findByCode("CEMENT").orElseThrow().getId();

        envelope(mvc.perform(get("/v2/api/inventory/items").with(adminAuth()).param("search", "cement").param("page", "1").param("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(1)));
        envelope(mvc.perform(get("/v2/api/inventory/items/{id}", itemId).with(adminAuth())).andExpect(status().isOk()));
        envelope(json(patch("/v2/api/inventory/items/{id}", itemId), "{\"code\":\"CEMENT-UPDATED\",\"name\":\"Cement updated\",\"category\":\"MATERIAL\",\"unit\":\"bag\",\"trackingMode\":\"QUANTITY\"}")
                .andExpect(status().isOk()));
        envelope(json(patch("/v2/api/inventory/items/{id}/status", itemId), "{\"active\":false}").andExpect(status().isOk()));
        envelope(mvc.perform(get("/v2/api/inventory/items/{id}/balance", itemId).with(adminAuth())).andExpect(status().isOk()));
    }

    @Test
    void locationEndpointsCoverCrudStatusBalanceAndValidation() throws Exception {
        envelope(json(post("/v2/api/inventory/locations"), "{\"code\":\"WH-1\",\"name\":\"Warehouse\",\"type\":\"WAREHOUSE\"}")
                .andExpect(status().isCreated()));
        Long locationId = locations.findByCode("WH-1").orElseThrow().getId();
        envelope(mvc.perform(get("/v2/api/inventory/locations").with(adminAuth()).param("type", "WAREHOUSE").param("page", "1").param("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(1)));
        envelope(mvc.perform(get("/v2/api/inventory/locations/{id}", locationId).with(adminAuth())).andExpect(status().isOk()));
        envelope(json(patch("/v2/api/inventory/locations/{id}", locationId), "{\"code\":\"WH-UPDATED\",\"name\":\"Updated warehouse\",\"type\":\"WAREHOUSE\"}")
                .andExpect(status().isOk()));
        envelope(json(patch("/v2/api/inventory/locations/{id}/status", locationId), "{\"active\":false}").andExpect(status().isOk()));
        envelope(mvc.perform(get("/v2/api/inventory/locations/{id}/balance", locationId).with(adminAuth())).andExpect(status().isOk()));
        json(post("/v2/api/inventory/locations"), "{\"code\":\"BAD\",\"name\":\"Bad\",\"type\":\"PROJECT_SITE\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
    }

    @Test
    void movementEndpointsCoverDirectionsPostingReversalBalancesAndConflicts() throws Exception {
        createItem("CEMENT", "QUANTITY");
        createLocation("WH-1");
        createLocation("WH-2");
        Long itemId = items.findByCode("CEMENT").orElseThrow().getId();
        Long sourceId = locations.findByCode("WH-1").orElseThrow().getId();
        Long targetId = locations.findByCode("WH-2").orElseThrow().getId();
        String receipt = "{\"type\":\"RECEIPT\",\"targetLocationId\":" + sourceId + ",\"lines\":[{\"itemId\":" + itemId + ",\"quantity\":10}]}";
        envelope(json(post("/v2/api/inventory/movements"), receipt).andExpect(status().isCreated()));
        Long receiptId = movements.findAll().getFirst().getId();
        envelope(mvc.perform(get("/v2/api/inventory/movements").with(adminAuth()).param("status", "DRAFT").param("itemId", itemId.toString()).param("page", "1").param("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(1)));
        envelope(mvc.perform(get("/v2/api/inventory/movements/{id}", receiptId).with(adminAuth())).andExpect(status().isOk()));
        envelope(mvc.perform(post("/v2/api/inventory/movements/{id}/post", receiptId).with(adminAuth()).with(csrf()).header("X-Trace-Id", "posting-trace"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("POSTED")));
        envelope(mvc.perform(get("/v2/api/inventory/items/{id}/balance", itemId).with(adminAuth())).andExpect(status().isOk()).andExpect(jsonPath("$.data.balances[0].quantity").value(10)));
        json(patch("/v2/api/inventory/movements/{id}", receiptId), receipt).andExpect(status().isConflict());
        envelope(mvc.perform(post("/v2/api/inventory/movements/{id}/reverse", receiptId).with(adminAuth()).with(csrf())).andExpect(status().isCreated()));
        json(post("/v2/api/inventory/movements"), "{\"type\":\"TRANSFER\",\"sourceLocationId\":" + sourceId + ",\"targetLocationId\":" + targetId + ",\"lines\":[{\"itemId\":" + itemId + ",\"quantity\":100}]}" )
                .andExpect(status().isCreated());
    }

    @Test
    void validationNotFoundForbiddenAndInsufficientStockAreEnforced() throws Exception {
        mvc.perform(get("/v2/api/inventory/items").with(user(operatorEmail).roles("PROJECT_MANAGER")))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.traceId").exists());
        json(post("/v2/api/inventory/items"), "{\"code\":\"\",\"name\":\"\",\"category\":\"MATERIAL\",\"unit\":\"bag\",\"trackingMode\":\"QUANTITY\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
        mvc.perform(get("/v2/api/inventory/items/999999").with(adminAuth())).andExpect(status().isNotFound()).andExpect(jsonPath("$.error").exists());
        createItem("CEMENT", "QUANTITY");
        createLocation("WH-1");
        Long itemId = items.findByCode("CEMENT").orElseThrow().getId();
        Long locationId = locations.findByCode("WH-1").orElseThrow().getId();
        json(post("/v2/api/inventory/movements"), "{\"type\":\"ISSUE\",\"sourceLocationId\":" + locationId + ",\"lines\":[{\"itemId\":" + itemId + ",\"quantity\":1}]}" )
                .andExpect(status().isCreated());
        Long movementId = movements.findAll().getFirst().getId();
        mvc.perform(post("/v2/api/inventory/movements/{id}/post", movementId).with(adminAuth()).with(csrf()))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.error").exists()).andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void adjustmentUsesDocumentedLocationIdAndSortsItems() throws Exception {
        createItem("ZINC", "QUANTITY");
        createItem("ALUMINUM", "QUANTITY");
        createLocation("WH-1");
        Long locationId = locations.findByCode("WH-1").orElseThrow().getId();
        Long itemId = items.findByCode("ZINC").orElseThrow().getId();

        envelope(json(post("/v2/api/inventory/movements"),
                "{\"type\":\"ADJUSTMENT\",\"locationId\":" + locationId
                        + ",\"adjustmentDirection\":\"INCREASE\",\"lines\":[{\"itemId\":"
                        + itemId + ",\"quantity\":3}]}")
                .andExpect(status().isCreated()));
        Long movementId = movements.findAll().getFirst().getId();
        envelope(mvc.perform(post("/v2/api/inventory/movements/{id}/post", movementId)
                .with(adminAuth()).with(csrf())).andExpect(status().isOk()));
        envelope(mvc.perform(get("/v2/api/inventory/items").with(adminAuth())
                .param("sort", "code,ASC").param("page", "1").param("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].code").value("ALUMINUM")));
    }

    private User seedUser(String emailPrefix, UserRole role) {
        User user = new User();
        user.setEmail(emailPrefix.contains("@") ? emailPrefix : emailPrefix + "@example.com");
        user.setFirstName("Inventory");
        user.setLastName("Tester");
        user.setPasswordHash(encoder.encode("password"));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(EnumSet.of(role));
        return users.save(user);
    }

    private void createItem(String code, String trackingMode) throws Exception {
        json(post("/v2/api/inventory/items"), "{\"code\":\"" + code + "\",\"name\":\"" + code + "\",\"category\":\"MATERIAL\",\"unit\":\"unit\",\"trackingMode\":\"" + trackingMode + "\"}")
                .andExpect(status().isCreated());
    }

    private void createLocation(String code) throws Exception {
        json(post("/v2/api/inventory/locations"), "{\"code\":\"" + code + "\",\"name\":\"" + code + "\",\"type\":\"WAREHOUSE\"}")
                .andExpect(status().isCreated());
    }

    private ResultActions json(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(request.with(adminAuth()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor adminAuth() {
        return user(admin.getEmail()).roles("COMPANY_ADMIN");
    }

    private void envelope(ResultActions result) throws Exception {
        result.andExpect(jsonPath("$.message").exists()).andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists()).andExpect(jsonPath("$.traceId").exists())
                .andExpect(header().exists("X-Trace-Id"));
    }
}
