package com.immopilot.modules.properties.repository;

import com.immopilot.modules.properties.domain.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {
    List<Property> findByUserId(Long userId);

    Page<Property> findByUserId(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    Optional<Property> findByIdAndUserId(Long id, Long userId);
}
