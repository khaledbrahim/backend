package com.immopilot.modules.sales.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleRecommendationDTO {
    private String action;
    private String reason;
    private String color;
}
