package com.immopilot.modules.finance.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class FinanceAnalyticsDTO {
    private BigDecimal roi; // Return on Investment %
    private BigDecimal netRoi; // Net ROI %
    private BigDecimal breakEvenPoint; // Total investment / Monthly Cashflow (months)
    private BigDecimal totalInvestment; // Property price + Works
    private Map<String, BigDecimal> monthlyProjections; // Next 12 months map (yyyy-MM -> amount)
}
