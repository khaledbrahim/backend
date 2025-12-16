package com.immopilot.modules.properties.service.dto;

import com.immopilot.modules.properties.domain.OwnershipType;
import com.immopilot.modules.properties.domain.PropertyStatus;
import com.immopilot.modules.properties.domain.PropertyType;
import com.immopilot.modules.properties.domain.RentalType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PropertyDTO {
    private Long id;
    private Long userId;
    private String name;
    private PropertyType propertyType;
    private RentalType rentalType;
    private OwnershipType ownershipType;
    private PropertyStatus status;
    private String address;
    private String city;
    private String country;
    private String zipCode;
    private Double area;
    private BigDecimal price;
    private BigDecimal marketValue;
    private LocalDate acquisitionDate;
    private Integer constructionYear;
    private Integer numberOfRooms;
    private String description;
    private String mainPhotoUrl;
    private java.util.List<String> photoUrls;
}
