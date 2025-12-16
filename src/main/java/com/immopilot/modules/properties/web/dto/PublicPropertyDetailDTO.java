package com.immopilot.modules.properties.web.dto;

import com.immopilot.modules.properties.domain.PropertyStatus;
import com.immopilot.modules.properties.domain.PropertyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PublicPropertyDetailDTO {
    private Long id;
    private String title;
    private String description;
    private String city;
    private String zipCode;
    private PropertyType type;
    private PropertyStatus status;
    private Double area;
    private Integer rooms;
    private BigDecimal price;
    private String mainPhotoUrl;
    // Extra fields for detail view
    private Integer constructionYear;
    private String country;
    // In real app, we would add list of photos, features, etc.
}
