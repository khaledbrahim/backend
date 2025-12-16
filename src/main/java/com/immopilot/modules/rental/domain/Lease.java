package com.immopilot.modules.rental.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.domain.PropertyUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "leases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnore
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    @JsonIgnore // Prevent recursion, DTO handles checking ID
    private PropertyUnit unit;

    @ManyToMany
    @JoinTable(name = "lease_tenants", joinColumns = @JoinColumn(name = "lease_id"), inverseJoinColumns = @JoinColumn(name = "tenant_id"))
    @JsonIgnore // Added JsonIgnore to prevent infinite recursion in JSON serialization
    @Builder.Default
    private Set<Tenant> tenants = new HashSet<>();

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "rent_amount", nullable = false)
    private BigDecimal rentAmount;

    @Column(name = "charges_amount")
    private BigDecimal chargesAmount;

    @Column(name = "deposit_amount")
    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaseStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentType paymentMethod;

    @Column(name = "irl_indexation")
    private Boolean irlIndexation = false;

    @Column(name = "duration")
    private Integer duration;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
