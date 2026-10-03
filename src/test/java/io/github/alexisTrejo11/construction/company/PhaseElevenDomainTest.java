package io.github.alexisTrejo11.construction.company;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.Budget;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.domain.BudgetStatus;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.Expense;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.ExpenseStatus;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryItem;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryMovement;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.InventoryMovementLine;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.domain.TrackingMode;
import io.github.alexisTrejo11.construction.company.modules.inventory.shared.dto.InventoryDtos.LineResponse;
import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.Notification;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhase;
import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhaseStatus;
import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.Project;
import io.github.alexisTrejo11.construction.company.modules.project.shared.domain.ProjectStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PhaseElevenDomainTest {
    @Test
    void projectAndPhaseRejectInvalidDatesAndTerminalTransitions() {
        Project project = new Project();
        project.setName("Project");
        project.setCode("P-11");
        project.setStartDate(LocalDate.of(2026, 1, 2));
        project.setEstimatedEndDate(LocalDate.of(2026, 1, 1));

        assertThat(project.validate().isSuccess()).isFalse();
        assertThat(project.updateStatus(ProjectStatus.IN_PROGRESS).isSuccess()).isTrue();
        assertThat(project.updateStatus(ProjectStatus.COMPLETED).isSuccess()).isTrue();
        assertThat(project.updateStatus(ProjectStatus.PLANNING).isSuccess()).isFalse();
        assertThat(project.getActualEndDate()).isNotNull();

        ProjectPhase phase = new ProjectPhase();
        phase.setName("Foundation");
        phase.setSequenceOrder(1);
        phase.setStartDate(LocalDate.of(2026, 2, 2));
        phase.setEndDate(LocalDate.of(2026, 2, 1));
        assertThat(phase.validate().isSuccess()).isFalse();
        assertThat(phase.updateStatus(ProjectPhaseStatus.COMPLETED).isSuccess()).isFalse();
    }

    @Test
    void budgetAndExpenseTransitionsOnlyAllowTheirDefinedLifecycle() {
        Budget budget = new Budget();
        budget.setCurrency("USD");
        assertThat(budget.approve().isSuccess()).isTrue();
        assertThat(budget.getStatus()).isEqualTo(BudgetStatus.APPROVED);
        assertThat(budget.approve().isSuccess()).isFalse();
        assertThat(budget.revise().isSuccess()).isTrue();
        assertThat(budget.close().isSuccess()).isFalse();
        assertThat(budget.approve().isSuccess()).isTrue();
        assertThat(budget.close().isSuccess()).isTrue();

        Expense expense = new Expense();
        expense.setAmount(new BigDecimal("12.50"));
        assertThat(expense.submit().isSuccess()).isTrue();
        assertThat(expense.approve().isSuccess()).isTrue();
        assertThat(expense.reject().isSuccess()).isFalse();
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
    }

    @Test
    void notificationReadIsIdempotentAndMovementKeepsLineOwnership() {
        Notification notification = new Notification();
        notification.markRead();
        Instant firstReadAt = notification.getReadAt();
        notification.markRead();
        assertThat(notification.getReadAt()).isEqualTo(firstReadAt);

        InventoryItem item = new InventoryItem();
        item.setId(7L);
        item.setCode("CEMENT");
        item.setTrackingMode(TrackingMode.SERIALIZED);
        InventoryMovementLine line = new InventoryMovementLine();
        line.setItem(item);
        line.setQuantity(BigDecimal.ONE);
        line.setSerialNumber("SERIAL-1");
        InventoryMovement movement = new InventoryMovement();
        movement.replaceLines(List.of(line));
        assertThat(line.getMovement()).isSameAs(movement);
        assertThat(new LineResponse(item.getId(), item.getCode(), line.getQuantity(), line.getSerialNumber()).serialNumber())
                .isEqualTo("SERIAL-1");
    }
}
