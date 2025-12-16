package com.immopilot.modules.construction.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ConstructionProgressLogDTO {
    private Long id;
    private Long lotId;
    private LocalDate logDate;
    private BigDecimal progress;
    private String description;
    private String photoUrl;
}
