package io.github.alexisTrejo11.construction.company.modules.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeAndPublicActuatorIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void homeIsPublicAndPresentsTheApi() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Trace-Id"))
            .andExpect(jsonPath("$.message").value("Welcome to the Construction Company API"))
            .andExpect(jsonPath("$.data.name").value("Construction Company API Test"))
            .andExpect(jsonPath("$.data.description").isNotEmpty())
            .andExpect(jsonPath("$.data.version").value("2.0.0"))
            .andExpect(jsonPath("$.data.apiBasePath").value("/v2/api"))
            .andExpect(jsonPath("$.data.links.documentation").value("/swagger-ui/index.html"))
            .andExpect(jsonPath("$.data.links.openApi").value("/api-docs"))
            .andExpect(jsonPath("$.data.links.health").value("/actuator/health"))
            .andExpect(jsonPath("$.error").doesNotExist())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void homeIsExcludedFromCsrfProtection() throws Exception {
        mockMvc.perform(post("/")
                .with(user("member@example.com").roles("COMPANY_ADMIN")))
            .andExpect(this::isNotRejectedByCsrf);
    }

    @Test
    void healthAndInfoActuatorEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/info"))
            .andExpect(status().isOk());
    }

    @Test
    void healthActuatorEndpointIsExcludedFromCsrfProtection() throws Exception {
        mockMvc.perform(post("/actuator/health")
                .with(user("member@example.com").roles("COMPANY_ADMIN")))
            .andExpect(this::isNotRejectedByCsrf);
    }

    @Test
    void protectedRouteStillRejectsMissingCsrfToken() throws Exception {
        mockMvc.perform(post("/v2/api/projects")
                .with(user("member@example.com").roles("COMPANY_ADMIN")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.error_type").value("FORBIDDEN"));
    }

    @Test
    void otherActuatorEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.error_type").value("UNAUTHENTICATED"))
            .andExpect(jsonPath("$.data").doesNotExist());

        mockMvc.perform(get("/actuator/metrics")
                .with(user("member@example.com").roles("COMPANY_ADMIN")))
            .andExpect(status().isOk());
    }

    private void isNotRejectedByCsrf(MvcResult result) {
        assertThat(result.getResponse().getStatus()).isNotEqualTo(HttpStatus.FORBIDDEN.value());
    }
}
