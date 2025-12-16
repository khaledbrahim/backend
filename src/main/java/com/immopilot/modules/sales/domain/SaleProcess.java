package com.immopilot.modules.sales.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale_processes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(name = "unit_id")
    private Long unitId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus status;

    @Column(name = "asking_price")
    private BigDecimal askingPrice;

    @Column(name = "net_price")
    private BigDecimal netPrice;

    @Column(name = "agency_fee")
    private BigDecimal agencyFee;

    @Column(name = "estimated_margin")
    private BigDecimal estimatedMargin;

    // Financial & Decision Support
    @Column(name = "acquisition_price")
    private BigDecimal acquisitionPrice;

    @Column(name = "total_works_amount")
    private BigDecimal totalWorksAmount;

    @Column(name = "total_charges_amount")
    private BigDecimal totalChargesAmount;

    @Column(name = "target_price")
    private BigDecimal targetPrice;

    @Column(name = "estimated_net_gain")
    private BigDecimal estimatedNetGain;

    @Column(name = "global_roi")
    private BigDecimal globalRoi;

    @Column(name = "abandon_reason", columnDefinition = "TEXT")
    private String abandonReason;

    @Column(name = "listing_date")
    private LocalDate listingDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null)
            status = SaleStatus.DRAFT;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
