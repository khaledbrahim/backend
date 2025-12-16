package com.immopilot.modules.finance.service.dto;

import com.immopilot.modules.finance.domain.OperationStatus;
import com.immopilot.modules.finance.domain.OperationType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class OperationResponse {
    private Long id;
    private Long propertyId;
    private String propertyName;
    private Long categoryId;
    private String categoryName;
    private OperationType operationType;
    private BigDecimal amount;
    private LocalDate operationDate;
    private String description;
    private String attachmentUrl;
    private Instant createdAt;
    private Instant updatedAt;

    private Long unitId;
    private String unitName;
    private String source;
    private String currency;
    private OperationStatus status;
    private String sourceReference;
}
