package com.immopilot.modules.visitorrequests.repository;

import com.immopilot.modules.visitorrequests.domain.VisitorRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitorRequestRepository
        extends JpaRepository<VisitorRequest, Long>, JpaSpecificationExecutor<VisitorRequest> {
    // Find requests for properties owned by a specific user (by email for
    // security/context)
    Page<VisitorRequest> findByProperty_User_Email(String email, Pageable pageable);
}
