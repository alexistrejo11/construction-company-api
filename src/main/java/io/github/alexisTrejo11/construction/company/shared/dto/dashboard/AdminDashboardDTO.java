package io.github.alexisTrejo11.construction.company.shared.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {
    @JsonProperty("pending_reimbursements")
    private int pendingReimbursements;

    @JsonProperty("pending_expenses")
    private int pendingExpenses;

    @JsonProperty("total_expenses")
    private int totalExpenses;

    @JsonProperty("total_approved_expenses")
    private int totalApprovedExpenses;

    @JsonProperty("total_rejected_expenses")
    private int totalRejectedExpenses;

    @JsonProperty("total_reimbursement_expenses")
    private int totalReimbursementExpenses;

}
