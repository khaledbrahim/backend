package com.immopilot.modules.properties.repository;

import com.immopilot.modules.properties.domain.PropertyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyDocumentRepository extends JpaRepository<PropertyDocument, Long> {
    List<PropertyDocument> findByPropertyId(Long propertyId);

    long countByPropertyId(Long propertyId);

    Optional<PropertyDocument> findByIdAndPropertyId(Long id, Long propertyId);
}
