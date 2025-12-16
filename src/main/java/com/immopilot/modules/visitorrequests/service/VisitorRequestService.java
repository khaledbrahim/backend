package com.immopilot.modules.visitorrequests.service;

import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.users.repository.UserRepository;
import com.immopilot.modules.visitorrequests.domain.VisitorRequest;
import com.immopilot.modules.visitorrequests.domain.VisitorRequestStatus;
import com.immopilot.modules.visitorrequests.repository.VisitorRequestRepository;
import com.immopilot.modules.visitorrequests.web.dto.VisitorRequestDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitorRequestService {

    private final VisitorRequestRepository requestRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Transactional
    public VisitorRequestDtos.RequestDetail createRequest(VisitorRequestDtos.CreateRequest dto, String userEmail) {
        var property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Use user details if not provided in DTO (or prioritize DTO for specific
        // contact info?)
        // Requirement: "informations minimales du visiteur (nom, email...)"
        // Let's use DTO fields if present, else fallback to User header
        String name = dto.getName() != null ? dto.getName() : (user.getFirstName() + " " + user.getLastName());
        String email = dto.getEmail() != null ? dto.getEmail() : user.getEmail();
        String phone = dto.getPhone() != null ? dto.getPhone() : user.getPhone();

        var request = VisitorRequest.builder()
                .property(property)
                .user(user)
                .visitorName(name)
                .visitorEmail(email)
                .visitorPhone(phone)
                .message(dto.getMessage())
                .type(dto.getType())
                .status(VisitorRequestStatus.NEW)
                .build();

        request = requestRepository.save(request);
        return mapToDetail(request);
    }

    public Page<VisitorRequestDtos.RequestDetail> getAllRequests(Pageable pageable) {
        return requestRepository.findAll(pageable).map(this::mapToDetail);
    }

    public Page<VisitorRequestDtos.RequestDetail> getOwnerRequests(String ownerEmail, Pageable pageable) {
        return requestRepository.findByProperty_User_Email(ownerEmail, pageable)
                .map(this::mapToDetail);
    }

    @Transactional
    public VisitorRequestDtos.RequestDetail updateStatus(Long id, VisitorRequestStatus newStatus, String userEmail) {
        var request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify ownership (or admin role - for MVP assuming owner)
        if (!request.getProperty().getUser().getEmail().equals(userEmail)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Not authorized to update this request");
        }

        if (request.getStatus() == VisitorRequestStatus.ACCEPTED
                || request.getStatus() == VisitorRequestStatus.REJECTED) {
            throw new IllegalStateException("Cannot change status of a finalized request (ACCEPTED or REJECTED).");
        }

        // Audit logic
        if (newStatus == VisitorRequestStatus.CONTACTED && request.getStatus() == VisitorRequestStatus.NEW) {
            request.setContactedAt(java.time.LocalDateTime.now());
        }

        request.setHandledBy(user);
        request.setStatus(newStatus);
        request = requestRepository.save(request);
        return mapToDetail(request);
    }

    private VisitorRequestDtos.RequestDetail mapToDetail(VisitorRequest r) {
        return VisitorRequestDtos.RequestDetail.builder()
                .id(r.getId())
                .propertyId(r.getProperty().getId())
                .propertyTitle(r.getProperty().getName())
                // .unitId(r.getUnit() != null ? r.getUnit().getId() : null) // Add to DTO later
                // if needed
                .visitorName(r.getVisitorName())
                .visitorEmail(r.getVisitorEmail())
                .message(r.getMessage())
                .type(r.getType())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .proposedPrice(r.getProposedPrice())
                .preferredDate(r.getPreferredDate())
                .build();
    }
}
