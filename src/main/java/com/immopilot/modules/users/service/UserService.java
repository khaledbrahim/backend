package com.immopilot.modules.users.service;

import com.immopilot.modules.users.domain.AuditLog;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.repository.AuditLogRepository;
import com.immopilot.modules.users.repository.UserRepository;
import com.immopilot.modules.users.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    // Inject EmailService when ready

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }
        // In a real JWT setup, principal is usually UserDetails or username
        String email;
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDto.UserProfileResponse getProfile() {
        User user = getCurrentUser();
        return mapToProfile(user);
    }

    @Transactional
    public UserDto.UserProfileResponse updateProfile(UserDto.UpdateProfileRequest request) {
        User user = getCurrentUser();

        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());
        if (request.getCountry() != null)
            user.setCountry(request.getCountry());
        if (request.getAvatarUrl() != null)
            user.setAvatarUrl(request.getAvatarUrl());

        userRepository.save(user);
        logAudit(user.getId(), "UPDATE_PROFILE", "Updated profile details");

        return mapToProfile(user);
    }

    @Transactional
    public UserDto.UserProfileResponse updateSettings(UserDto.UpdateSettingsRequest request) {
        User user = getCurrentUser();

        if (request.getLanguage() != null)
            user.setLanguage(request.getLanguage());
        if (request.getTimezone() != null)
            user.setTimezone(request.getTimezone());
        if (request.getEmailNotifications() != null)
            user.setEmailNotifications(request.getEmailNotifications());

        userRepository.save(user);
        logAudit(user.getId(), "UPDATE_SETTINGS", "Updated account settings");

        return mapToProfile(user);
    }

    @Transactional
    public void changePassword(UserDto.ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid old password");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("New password cannot be the same as old password");
        }

        // Add complexity check here if not enforcing via annotation/frontend only

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logAudit(user.getId(), "CHANGE_PASSWORD", "Password changed");
    }

    @Transactional
    public void requestEmailChange(UserDto.ChangeEmailRequest request) {
        User user = getCurrentUser();
        String newEmail = request.getNewEmail();

        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email already in use");
        }

        // In a real flow:
        // 1. Send verification email to NEW email with a token.
        // 2. Do NOT update email yet.
        // 3. User clicks link -> Verify token -> Update email.

        // For Proof of Concept / MVP shortcut, we might just update it and set
        // unverified?
        // But safer to verify first.
        // Let's implement immediate update + unverify for simplicity in this MVP
        // iteration,
        // but ideally we'd use a temporary "new_email" field.
        // "Le changement d’email déclenche obligatoirement une reverification par lien
        // sécurisé"

        // Implementation:
        // Set unverified. User must re-verify.
        user.setEmail(newEmail);
        user.setVerified(false);
        user.setActivationToken(UUID.randomUUID().toString());
        user.setRegistrationStatus(com.immopilot.modules.users.domain.RegistrationStatus.PENDING_VERIFICATION);

        userRepository.save(user);
        logAudit(user.getId(), "REQUEST_EMAIL_CHANGE", "Changed email to " + newEmail + ", pending verification");

        // Mock email send
        System.out.println("Sending re-verification to " + newEmail + " token: " + user.getActivationToken());
    }

    @Transactional
    public void deleteAccount() {
        User user = getCurrentUser();
        user.setDeleted(true);
        user.setDeletedAt(Instant.now());
        // Might also want to anonymize data or disable login immediately (via security
        // filter check)
        userRepository.save(user);
        logAudit(user.getId(), "DELETE_ACCOUNT", "Soft deleted account");
    }

    private void logAudit(Long userId, String action, String details) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .details(details)
                .build(); // ipAddress handled by controller/filter ideally
        auditLogRepository.save(log);
    }

    private UserDto.UserProfileResponse mapToProfile(User user) {
        return UserDto.UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .country(user.getCountry())
                .language(user.getLanguage())
                .timezone(user.getTimezone())
                .profileType(user.getProfileType())
                .registrationStatus(user.getRegistrationStatus())
                .isVerified(user.isVerified())
                .emailNotifications(user.isEmailNotifications())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
