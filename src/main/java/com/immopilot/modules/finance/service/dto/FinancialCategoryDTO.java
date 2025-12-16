package com.immopilot.modules.finance.service.dto;

import com.immopilot.modules.finance.domain.OperationType;
import com.immopilot.modules.properties.domain.PropertyType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinancialCategoryDTO {
    private Long id;
    private String name;
    private OperationType type;
    private boolean isSystemDefault;
    private java.util.Set<PropertyType> applicablePropertyTypes;
}
