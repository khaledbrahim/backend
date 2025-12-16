package com.immopilot.modules.users.service.dto;

import com.immopilot.modules.users.domain.ProfileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegistrationStep1Request {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String phone;
        private String country;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegistrationStep2Request {
        private String email; // to identify user
        private ProfileType profileType;
        // extended fields can be added here
        private String siret; // example for Agency
        private String companyName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegistrationStep3Request {
        private String email;
        private Long planId;
        private String paymentToken; // for premium plans
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthenticationRequest {
        private String email;
        private String password;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthenticationResponse {
        private String token;
        private String[] roles;
        private Object menu; // Dynamic menu simplified for now
    }
}
