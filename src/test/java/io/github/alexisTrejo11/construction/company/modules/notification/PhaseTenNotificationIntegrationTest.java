package io.github.alexisTrejo11.construction.company.modules.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.Notification;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.NotificationType;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.persistence.NotificationRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserStatus;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PhaseTenNotificationIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private NotificationRepository notifications;
    @Autowired private PasswordEncoder encoder;

    @MockitoBean private JavaMailSender mailSender;

    private User owner;
    private User other;

    @BeforeEach
    void setUp() {
        notifications.deleteAll();
        owner = seed("notification-owner-" + System.nanoTime() + "@example.com");
        other = seed("notification-other-" + System.nanoTime() + "@example.com");
    }

    @Test
    void listDetailReadAndReadAllAreScopedAndEnveloped() throws Exception {
        Notification unread = notification(owner, "Unread");
        Notification read = notification(owner, "Read");
        read.markRead();
        notification(other, "Private");
        notifications.saveAll(java.util.List.of(unread, read));

        mvc.perform(get("/v2/api/notifications").with(auth()).param("read", "false").param("page", "1").param("size", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(1))
            .andExpect(jsonPath("$.data.page").value(1)).andExpect(jsonPath("$.data.totalItems").value(1))
            .andExpect(jsonPath("$.message").exists()).andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.traceId").exists()).andExpect(header().exists("X-Trace-Id"));
        mvc.perform(get("/v2/api/notifications/{id}", unread.getId()).with(auth()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.title").value("Unread"));
        mvc.perform(get("/v2/api/notifications/{id}", read.getId()).with(otherAuth()))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.error_type").value("NOT_FOUND"));
        mvc.perform(patch("/v2/api/notifications/{id}/read", unread.getId()).with(auth()).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.readAt").exists());
        mvc.perform(patch("/v2/api/notifications/{id}/read", unread.getId()).with(auth()).with(csrf()))
            .andExpect(status().isOk());
        mvc.perform(patch("/v2/api/notifications/read-all").with(auth()).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.newlyMarkedCount").value(0));
    }

    @Test
    void readAllReturnsNewlyMarkedCountAndPatchRequiresCsrf() throws Exception {
        notifications.save(notification(owner, "Unread"));
        mvc.perform(patch("/v2/api/notifications/read-all").with(auth()))
            .andExpect(status().isForbidden());
        mvc.perform(patch("/v2/api/notifications/read-all").with(auth()).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.newlyMarkedCount").value(1));
    }

    @Test
    void invitationCreatesPostCommitNotificationAndAsynchronousEmail() throws Exception {
        mvc.perform(post("/v2/api/invitations").with(user(owner.getEmail()).roles("COMPANY_ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"post-commit-" + System.nanoTime() + "@example.com\",\"roles\":[\"SITE_ENGINEER\"]}"))
            .andExpect(status().isCreated());
        await(() -> notifications.count() == 1);
        verify(mailSender, org.mockito.Mockito.timeout(2000)).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    private User seed(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode("password"));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(EnumSet.of(UserRole.COMPANY_ADMIN));
        return users.save(user);
    }

    private Notification notification(User recipient, String title) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(NotificationType.INVITATION);
        notification.setTitle(title);
        notification.setMessage("Message");
        return notification;
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor auth() {
        return user(owner.getEmail()).roles("COMPANY_ADMIN");
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor otherAuth() {
        return user(other.getEmail()).roles("COMPANY_ADMIN");
    }

    private void await(BooleanSupplier condition) throws InterruptedException {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(2));
        while (!condition.getAsBoolean() && Instant.now().isBefore(deadline)) {
            Thread.sleep(25);
        }
        assertThat(condition.getAsBoolean()).isTrue();
    }
}
