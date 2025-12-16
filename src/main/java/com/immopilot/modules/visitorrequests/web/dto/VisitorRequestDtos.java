package com.immopilot.modules.visitorrequests.web.dto;

import com.immopilot.modules.visitorrequests.domain.VisitorRequestStatus;
import com.immopilot.modules.visitorrequests.domain.VisitorRequestType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VisitorRequestDtos {

    @Data
    @Builder
    public static class CreateRequest {
        private Long propertyId;
        private Long userId; // Optional, can be inferred from Auth
        private String message;
        private VisitorRequestType type;
        // User contact info might be taken from User entity or overridden here
        private String name;
        private String phone;
        private String email;
        private BigDecimal proposedPrice;
        private LocalDateTime preferredDate;
    }

    @Data
    @Builder
    public static class RequestDetail {
        private Long id;
        private Long propertyId;
        private String propertyTitle; // Helper for UI
        private String visitorName;
        private String visitorEmail;
        private String message;
        private VisitorRequestType type;
        private VisitorRequestStatus status;
        private LocalDateTime createdAt;
        private BigDecimal proposedPrice;
        private LocalDateTime preferredDate;
    }
}
