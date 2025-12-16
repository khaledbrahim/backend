package com.immopilot.modules.rental.repository;

import com.immopilot.modules.rental.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    List<Tenant> findByPropertyId(Long propertyId);
}
