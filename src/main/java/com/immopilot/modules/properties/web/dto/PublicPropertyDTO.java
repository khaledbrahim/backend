package com.immopilot.modules.properties.web.dto;

import com.immopilot.modules.properties.domain.PropertyStatus;
import com.immopilot.modules.properties.domain.PropertyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PublicPropertyDTO {
    private Long id;
    private String title; // name
    private String description;
    private String city;
    private String zipCode;
    private PropertyType type;
    private PropertyStatus status;
    private Double area;
    private Integer rooms; // numberOfRooms
    private BigDecimal price; // price or marketValue
    private String mainPhotoUrl;
}
