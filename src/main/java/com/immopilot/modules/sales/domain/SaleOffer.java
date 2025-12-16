package com.immopilot.modules.sales.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_id", nullable = false)
    private Long processId;

    @Column(name = "prospect_id", nullable = false)
    private Long prospectId;

    @Column(name = "offer_date")
    private LocalDateTime offerDate;

    @Column(name = "offer_amount", nullable = false)
    private BigDecimal offerAmount;

    private String conditions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    @Column(name = "validity_date")
    private LocalDate validityDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prospect_id", insertable = false, updatable = false)
    private SaleProspect prospect;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (offerDate == null)
            offerDate = LocalDateTime.now();
        if (status == null)
            status = OfferStatus.PENDING;
    }
}
