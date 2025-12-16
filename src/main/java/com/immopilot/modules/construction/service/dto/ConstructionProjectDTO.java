package com.immopilot.modules.construction.service.dto;

import com.immopilot.modules.construction.domain.ConstructionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ConstructionProjectDTO {
    private Long id;
    private Long propertyId;
    private String name;
    private ConstructionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budgetTotal;
    private BigDecimal progress;
    private List<ConstructionLotDTO> lots;
}
