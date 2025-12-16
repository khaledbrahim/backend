package com.immopilot.modules.properties.service.dto;

import com.immopilot.modules.properties.domain.OwnershipType;
import com.immopilot.modules.properties.domain.PropertyStatus;
import com.immopilot.modules.properties.domain.PropertyType;
import com.immopilot.modules.properties.domain.RentalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePropertyRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    private RentalType rentalType;
    private OwnershipType ownershipType;

    @NotNull(message = "Status is required")
    private PropertyStatus status;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    private String country;

    @PositiveOrZero(message = "Area cannot be negative")
    private Double area;

    @PositiveOrZero(message = "Price cannot be negative")
    private BigDecimal price;

    @PositiveOrZero(message = "Market value cannot be negative")
    private BigDecimal marketValue;
    private LocalDate acquisitionDate;
    private String description;

    @PositiveOrZero(message = "Number of units cannot be negative")
    private Integer numberOfUnits;
}
