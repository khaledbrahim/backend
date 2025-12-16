package com.immopilot.modules.rental.service.dto;

import com.immopilot.modules.rental.domain.LeaseStatus;
import com.immopilot.modules.rental.domain.PaymentFrequency;
import com.immopilot.modules.rental.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaseDTO {
    private Long id;
    private Long propertyId;
    private Long unitId; // Optional, for multi-unit properties
    private List<Long> tenantIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal rentAmount;
    private BigDecimal chargesAmount;
    private BigDecimal depositAmount;
    private PaymentFrequency frequency;
    private PaymentType paymentMethod;
    private Boolean irlIndexation;
    private Integer duration;
    private LeaseStatus status;

    // Calculated fields
    private LocalDate nextPaymentDate;
    private LocalDate lastPaymentDate;
    private BigDecimal nextPaymentAmount; // Total due
    private Boolean isLate; // If nextPaymentDate < now and not paid
    private Boolean isUpToDate; // True if no overdue payments
    private Boolean isGlobalLease; // True if lease covers entire property despite having units
}
