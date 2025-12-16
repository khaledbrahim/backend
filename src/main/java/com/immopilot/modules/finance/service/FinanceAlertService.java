package com.immopilot.modules.finance.service;

import com.immopilot.modules.finance.repository.FinancialOperationRepository;
import com.immopilot.modules.finance.service.dto.CashflowStats;
import com.immopilot.modules.finance.service.dto.FinancialAlertDTO;
import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceAlertService {

    private final FinancialOperationRepository operationRepository;
    private final PropertyRepository propertyRepository;
    private final FinanceService financeService; // Reuse getStats logic
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<FinancialAlertDTO> getAlerts() {
        User user = userService.getCurrentUser();
        List<FinancialAlertDTO> alerts = new ArrayList<>();
        List<Property> properties = propertyRepository.findByUserId(user.getId());

        for (Property property : properties) {
            checkNegativeCashflow(property, alerts);
            checkExpenseExceedsRevenue(property, alerts);
            // checkBudgetOverrun(property, alerts); // Placeholder for future feature
        }

        return alerts;
    }

    private void checkNegativeCashflow(Property property, List<FinancialAlertDTO> alerts) {
        // Check last 2 months
        LocalDate now = LocalDate.now();
        int negativeMonths = 0;

        for (int i = 1; i <= 2; i++) {
            LocalDate date = now.minusMonths(i);
            CashflowStats stats = financeService.getStats(property.getId(), date.getYear(), date.getMonthValue());
            if (stats.getCashflow().compareTo(BigDecimal.ZERO) < 0) {
                negativeMonths++;
            }
        }

        if (negativeMonths >= 2) {
            alerts.add(FinancialAlertDTO.builder()
                    .type("NEGATIVE_CASHFLOW")
                    .message("Cashflow négatif détecté sur les 2 derniers mois.")
                    .severity("CRITICAL")
                    .propertyId(property.getId())
                    .propertyName(property.getName())
                    .build());
        }
    }

    private void checkExpenseExceedsRevenue(Property property, List<FinancialAlertDTO> alerts) {
        // Check current month
        LocalDate now = LocalDate.now();
        CashflowStats stats = financeService.getStats(property.getId(), now.getYear(), now.getMonthValue());

        if (stats.getTotalExpense().compareTo(stats.getTotalRevenue()) > 0) {
            alerts.add(FinancialAlertDTO.builder()
                    .type("EXPENSE_WARNING")
                    .message("Les dépenses dépassent les revenus ce mois-ci.")
                    .severity("WARNING")
                    .propertyId(property.getId())
                    .propertyName(property.getName())
                    .build());
        }
    }
}
