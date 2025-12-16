package com.immopilot.modules.finance.service;

import com.immopilot.modules.finance.service.dto.CashflowStats;
import com.immopilot.modules.finance.service.dto.FinanceAnalyticsDTO;
import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceAnalyticsService {

    private final PropertyRepository propertyRepository;
    private final FinanceService financeService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public FinanceAnalyticsDTO getAnalytics(Long propertyId) {
        User user = userService.getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Calculate annual stats (last 12 months)
        BigDecimal annualRevenue = BigDecimal.ZERO;
        BigDecimal annualExpense = BigDecimal.ZERO;
        LocalDate now = LocalDate.now();

        for (int i = 0; i < 12; i++) {
            LocalDate date = now.minusMonths(i);
            CashflowStats stats = financeService.getStats(propertyId, date.getYear(), date.getMonthValue());
            annualRevenue = annualRevenue.add(stats.getTotalRevenue());
            annualExpense = annualExpense.add(stats.getTotalExpense());
        }

        BigDecimal annualCashflow = annualRevenue.subtract(annualExpense);
        BigDecimal totalInvestment = property.getPrice() != null ? property.getPrice() : BigDecimal.ZERO;
        // Ideally add works cost from category 'Works' but simplified here

        BigDecimal roi = BigDecimal.ZERO;
        BigDecimal netRoi = BigDecimal.ZERO;
        BigDecimal breakEvenPoint = BigDecimal.ZERO;

        if (totalInvestment.compareTo(BigDecimal.ZERO) > 0) {
            // Gross ROI = (Annual Revenue / Total Investment) * 100
            roi = annualRevenue.divide(totalInvestment, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

            // Net ROI = (Annual Cashflow / Total Investment) * 100
            netRoi = annualCashflow.divide(totalInvestment, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

            // Break-even (years) = Total Investment / Annual Cashflow
            if (annualCashflow.compareTo(BigDecimal.ZERO) > 0) {
                breakEvenPoint = totalInvestment.divide(annualCashflow, 2, RoundingMode.HALF_UP);
            }
        }

        // Projections (Simple Linear Trend based on last 12 mo avg)
        Map<String, BigDecimal> projections = new LinkedHashMap<>();
        BigDecimal avgMonthlyCashflow = annualCashflow.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);

        for (int i = 1; i <= 12; i++) {
            LocalDate futureDate = now.plusMonths(i);
            String monthKey = futureDate.toString().substring(0, 7);
            projections.put(monthKey, avgMonthlyCashflow); // Constant projection for Standard plan
        }

        return FinanceAnalyticsDTO.builder()
                .roi(roi)
                .netRoi(netRoi)
                .breakEvenPoint(breakEvenPoint)
                .totalInvestment(totalInvestment)
                .monthlyProjections(projections)
                .build();
    }
}
