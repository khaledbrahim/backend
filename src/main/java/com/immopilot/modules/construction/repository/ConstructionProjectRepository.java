package com.immopilot.modules.construction.repository;

import com.immopilot.modules.construction.domain.ConstructionProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstructionProjectRepository extends JpaRepository<ConstructionProject, Long> {
    List<ConstructionProject> findByPropertyId(Long propertyId);
}
