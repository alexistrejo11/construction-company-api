package io.github.alexisTrejo11.construction.company.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.alexisTrejo11.construction.company.modules.budget.features.approve.ApproveBudgetHandler;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.mapper.BudgetMapper;
import io.github.alexisTrejo11.construction.company.modules.budget.shared.persistence.BudgetRepository;
import io.github.alexisTrejo11.construction.company.modules.expense.features.submit.SubmitExpenseHandler;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.mapper.ExpenseMapper;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.persistence.ExpenseRepository;
import io.github.alexisTrejo11.construction.company.modules.user.features.createinvitation.CreateInvitationHandler;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.InvitationRepository;
import io.github.alexisTrejo11.construction.company.modules.user.shared.persistence.UserRepository;
import io.github.alexisTrejo11.construction.company.modules.project.shared.policy.ProjectAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.GlobalAuthorizationPolicy;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class HandlerResultTest {
    private final UserContext user = new UserContext(1L, "handler@example.com", Set.of(), Set.of());

    @Test
    void invitationReturnsAuthorizationResultBeforeRepositoryWork() {
        GlobalAuthorizationPolicy policy = mock(GlobalAuthorizationPolicy.class);
        when(policy.requirePermission(user, Permission.USER_INVITE)).thenReturn(Result.forbidden("denied"));
        CreateInvitationHandler handler = new CreateInvitationHandler(
                mock(UserRepository.class), mock(InvitationRepository.class),
                mock(ApplicationEventPublisher.class), policy);

        Result<?> result = handler.execute(user, null);

        assertThat(result.getErrorType()).isEqualTo(Result.ErrorType.FORBIDDEN);
    }

    @Test
    void budgetApprovalReturnsNotFoundWithoutAuthorizationOrMapping() {
        BudgetRepository repository = mock(BudgetRepository.class);
        when(repository.findById(9L)).thenReturn(Optional.empty());

        Result<?> result = new ApproveBudgetHandler(
                repository, mock(ProjectAuthorizationPolicy.class),
                mock(BudgetMapper.class)).handle(user, 9L);

        assertThat(result.getErrorType()).isEqualTo(Result.ErrorType.NOT_FOUND);
    }

    @Test
    void expenseSubmissionReturnsNotFoundBeforeTransition() {
        ExpenseRepository repository = mock(ExpenseRepository.class);
        when(repository.findById(9L)).thenReturn(Optional.empty());

        Result<?> result = new SubmitExpenseHandler(
                repository, mock(ProjectAuthorizationPolicy.class),
                mock(ExpenseMapper.class)).handle(user, 9L);

        assertThat(result.getErrorType()).isEqualTo(Result.ErrorType.NOT_FOUND);
    }
}
