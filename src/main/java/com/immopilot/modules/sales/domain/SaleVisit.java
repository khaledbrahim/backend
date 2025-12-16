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

import java.time.LocalDateTime;

@Entity
@Table(name = "sale_visits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_id", nullable = false)
    private Long processId;

    @Column(name = "prospect_id", nullable = false)
    private Long prospectId;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Column(name = "visit_type")
    private String visitType; // Can be Enum later if needed

    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_level")
    private VisitInterest interestLevel;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prospect_id", insertable = false, updatable = false)
    private SaleProspect prospect;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
