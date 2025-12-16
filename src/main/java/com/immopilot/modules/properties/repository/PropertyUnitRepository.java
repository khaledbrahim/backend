package com.immopilot.modules.properties.repository;

import com.immopilot.modules.properties.domain.PropertyUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyUnitRepository extends JpaRepository<PropertyUnit, Long> {
    List<PropertyUnit> findAllByPropertyId(Long propertyId);
}
