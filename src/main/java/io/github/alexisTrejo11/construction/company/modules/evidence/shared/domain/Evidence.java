package io.github.alexisTrejo11.construction.company.modules.evidence.shared.domain;

import io.github.alexisTrejo11.construction.company.modules.project.phases.shared.domain.ProjectPhase;
import io.github.alexisTrejo11.construction.company.modules.expense.shared.domain.Expense;
import io.github.alexisTrejo11.construction.company.modules.user.shared.domain.User;
import io.github.alexisTrejo11.construction.company.shared.persistence.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "evidence")
@Check(constraints = "(phase_id IS NOT NULL AND expense_id IS NULL) OR (phase_id IS NULL AND expense_id IS NOT NULL)")
@Getter
@Setter
public class Evidence extends AbstractJpaEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id")
    private ProjectPhase phase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
}
