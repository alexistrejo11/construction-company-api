package io.github.alexisTrejo11.construction.company.shared.dto.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DashboardStatsDTO {

    private int totalExpenses;
    private int pendingExpenses;
    private int totalRejectedExpenses;
    private int totalApprovedExpenses;
    private int totalReimbursementExpenses;

    public DashboardStatsDTO(int totalExpenses, int pendingExpenses, int totalRejectedExpenses, int totalApprovedExpenses, int totalReimbursementExpenses) {
        this.totalExpenses = totalExpenses;
        this.pendingExpenses = pendingExpenses;
        this.totalRejectedExpenses = totalRejectedExpenses;
        this.totalApprovedExpenses = totalApprovedExpenses;
        this.totalReimbursementExpenses = totalReimbursementExpenses;
    }
}
