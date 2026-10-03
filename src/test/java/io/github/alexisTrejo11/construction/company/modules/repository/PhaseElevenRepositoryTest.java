package io.github.alexisTrejo11.construction.company.modules.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.Budget;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetRepository;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.Expense;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence.ExpenseRepository;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryItem;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryCategory;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.TrackingMode;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.persistence.InventoryItemRepository;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.Notification;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.NotificationType;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.persistence.NotificationRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import io.github.alexisTrejo11.construction.company.modules.project.shared.persistence.ProjectRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import java.math.BigDecimal;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class PhaseElevenRepositoryTest {
    @Autowired private UserRepository users;
    @Autowired private ProjectRepository projects;
    @Autowired private NotificationRepository notifications;
    @Autowired private InventoryItemRepository items;
    @Autowired private BudgetRepository budgets;
    @Autowired private ExpenseRepository expenses;

    @Test
    void notificationQueriesAreOwnedAndUnreadScoped() {
        User owner = users.save(user("repo-owner@example.com"));
        User other = users.save(user("repo-other@example.com"));
        Notification ownerNotification = notifications.save(notification(owner, "owner"));
        notifications.save(notification(other, "other"));

        assertThat(notifications.findByRecipientId(owner.getId(), org.springframework.data.domain.PageRequest.of(0, 10)))
                .hasSize(1);
        assertThat(notifications.findByIdAndRecipientId(ownerNotification.getId(), other.getId())).isEmpty();
    }

    @Test
    void inventoryBudgetAndExpenseRepositoriesPersistRelationships() {
        InventoryItem item = new InventoryItem();
        item.setCode("REPO-ITEM");
        item.setName("Repository item");
        item.setCategory(InventoryCategory.MATERIAL);
        item.setUnit("unit");
        item.setTrackingMode(TrackingMode.QUANTITY);
        assertThat(items.save(item).getId()).isNotNull();

        Project project = new Project();
        project.setName("Repository project");
        project.setCode("REPO-PROJECT");
        project = projects.save(project);
        Budget budget = new Budget();
        budget.setProject(project);
        budget.setCurrency("USD");
        budget = budgets.save(budget);
        Expense expense = new Expense();
        expense.setProject(project);
        expense.setBudget(budget);
        expense.setAmount(new BigDecimal("5.00"));
        expenses.save(expense);

        assertThat(budgets.findByProjectId(project.getId())).contains(budget);
        assertThat(expenses.findByBudgetIdOrderByCreatedAtDesc(budget.getId())).hasSize(1);
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setRoles(EnumSet.noneOf(io.github.alexisTrejo11.construction.company.modules.user.shared.domain.UserRole.class));
        return user;
    }

    private Notification notification(User recipient, String title) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(NotificationType.INVITATION);
        notification.setTitle(title);
        notification.setMessage("message");
        return notification;
    }
}
