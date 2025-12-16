package com.immopilot.modules.users.service.dto;

import com.immopilot.modules.users.domain.ProfileType;
import com.immopilot.modules.users.domain.RegistrationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

public class UserDto {

    @Data
    @Builder
    public static class UserProfileResponse {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String country;
        private String language;
        private String timezone;
        private ProfileType profileType;
        private RegistrationStatus registrationStatus;
        private boolean isVerified;
        private boolean emailNotifications;
        private String avatarUrl;
        private Instant createdAt;
    }

    @Data
    @Builder
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String phone;
        private String country;
        private String avatarUrl;
    }

    @Data
    @Builder
    public static class UpdateSettingsRequest {
        private String language;
        private String timezone;
        private Boolean emailNotifications;
    }

    @Data
    @Builder
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }

    @Data
    @Builder
    public static class ChangeEmailRequest {
        private String newEmail;
    }
}
