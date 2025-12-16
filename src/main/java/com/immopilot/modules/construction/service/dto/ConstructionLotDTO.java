package com.immopilot.modules.construction.service.dto;

import com.immopilot.modules.construction.domain.ConstructionLotType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConstructionLotDTO {
    private Long id;
    private Long projectId;
    private String name;
    private ConstructionLotType lotType;
    private BigDecimal budgetExpected;
    private BigDecimal budgetReal;
    private BigDecimal progress;
    private String notes;
}
