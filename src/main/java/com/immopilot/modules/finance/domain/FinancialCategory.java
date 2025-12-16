package com.immopilot.modules.finance.domain;

import com.immopilot.modules.users.domain.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "financial_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FinancialCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Nullable for system default categories

    @Column(name = "is_system_default")
    @Builder.Default
    private boolean isSystemDefault = false;

    @ElementCollection(targetClass = com.immopilot.modules.properties.domain.PropertyType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "financial_category_property_types", joinColumns = @JoinColumn(name = "category_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "property_type")
    private java.util.Set<com.immopilot.modules.properties.domain.PropertyType> applicablePropertyTypes;

    @CreatedDate
    private Instant createdAt;
}
