package com.immopilot.modules.finance.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class CashflowStats {
    private BigDecimal totalRevenue;
    private BigDecimal totalExpense;
    private BigDecimal cashflow; // Revenue - Expense
    private Map<String, BigDecimal> expenseByCategory;

    // Monthly breakdown for charts (Key: "yyyy-MM")
    private Map<String, BigDecimal> monthlyRevenue;
    private Map<String, BigDecimal> monthlyExpense;
    private Map<String, BigDecimal> monthlyCashflow;
}
