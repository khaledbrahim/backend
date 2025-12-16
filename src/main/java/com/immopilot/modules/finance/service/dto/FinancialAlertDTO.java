package com.immopilot.modules.finance.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinancialAlertDTO {
    private String type; // e.g., "NEGATIVE_CASHFLOW", "BUDGET_OVERRUN"
    private String message;
    private String severity; // "WARNING", "CRITICAL", "INFO"
    private Long propertyId;
    private String propertyName;
}
