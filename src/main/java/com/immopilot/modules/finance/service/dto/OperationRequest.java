package com.immopilot.modules.finance.service.dto;

import com.immopilot.modules.finance.domain.OperationStatus;
import com.immopilot.modules.finance.domain.OperationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OperationRequest {
    @NotNull
    private Long propertyId;

    private Long categoryId; // Optional if we want to allow skipping category or auto-assign

    @NotNull
    private OperationType operationType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate operationDate;

    private String description;

    private Long unitId;

    private String source; // "MANUAL", "OCR", etc.

    private String currency; // Default EUR
    private OperationStatus status; // Default CONFIRMED
    private String sourceReference;
}
