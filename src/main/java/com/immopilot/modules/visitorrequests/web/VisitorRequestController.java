package com.immopilot.modules.visitorrequests.web;

import com.immopilot.modules.visitorrequests.domain.VisitorRequestStatus;
import com.immopilot.modules.visitorrequests.service.VisitorRequestService;
import com.immopilot.modules.visitorrequests.web.dto.VisitorRequestDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class VisitorRequestController {

    private final VisitorRequestService requestService;

    @PostMapping
    public ResponseEntity<VisitorRequestDtos.RequestDetail> createRequest(
            @RequestBody VisitorRequestDtos.CreateRequest createDto,
            Authentication authentication) {
        return ResponseEntity.ok(requestService.createRequest(createDto, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Page<VisitorRequestDtos.RequestDetail>> getAllRequests(Pageable pageable) {
        return ResponseEntity.ok(requestService.getAllRequests(pageable));
    }

    @GetMapping("/owner")
    public ResponseEntity<Page<VisitorRequestDtos.RequestDetail>> getOwnerRequests(
            Authentication authentication,
            Pageable pageable) {
        return ResponseEntity.ok(requestService.getOwnerRequests(authentication.getName(), pageable));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VisitorRequestDtos.RequestDetail> updateStatus(
            @PathVariable Long id,
            @RequestParam VisitorRequestStatus status,
            Authentication authentication) {
        return ResponseEntity.ok(requestService.updateStatus(id, status, authentication.getName()));
    }
}
