package com.immopilot.modules.properties.service.dto;

import com.immopilot.modules.properties.domain.PropertyType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PropertyUnitDTO {
    private Long id;
    private Long propertyId;
    private String name;
    private PropertyType type;
    private Double area;
    private Double shares;
    private String description;
}
