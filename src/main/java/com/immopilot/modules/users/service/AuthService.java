package com.immopilot.modules.users.service;

import com.immopilot.infrastructure.security.JwtService;
import com.immopilot.modules.users.domain.ProfileType;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.repository.UserRepository;
import com.immopilot.modules.users.service.dto.AuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDto.AuthenticationResponse registerStep1(AuthDto.RegistrationStep1Request request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                // .country(request.getCountry()) // Assuming User entity will have country
                // later, or ignored for now
                .profileType(ProfileType.NOT_SELECTED)
                .registrationStatus(
                        com.immopilot.modules.users.domain.RegistrationStatus.PENDING_VERIFICATION)
                .roles(new java.util.HashSet<>()) // No roles yet, or maybe ROLE_GUEST?
                .isVerified(false)
                .activationToken(java.util.UUID.randomUUID().toString())
                .build();

        userRepository.save(user);

        // TODO: Send verification email with user.getActivationToken()
        System.out.println(
                "Sending verification email to " + user.getEmail() + " with token: "
                        + user.getActivationToken());

        // Return a response indicating pending verification, not a full login token yet
        // OR return a token with limited scope?
        // For this step, we probably just return success message or a temporary token
        // if we want to allow "partial" login state.
        // But per requirements: "The back ... stores user in PENDING_VERIFICATION ...
        // generates activation token ... triggers email."
        // So we likely just return void or a simple success DTO.
        // However, the existing signature returned AuthenticationResponse.
        // Let's return a dummy response or create a new one.
        // For now, let's keep AuthenticationResponse but with empty token/roles to
        // imply "no login yet".

        return AuthDto.AuthenticationResponse.builder()
                .token(null)
                .roles(new String[]{})
                .build();
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));

        user.setVerified(true);
        user.setActivationToken(null); // Invalidate token
        if (user.getRegistrationStatus() == com.immopilot.modules.users.domain.RegistrationStatus.PENDING_VERIFICATION) {
            // Or maybe we don't change status heavily here, just verified.
            // But if they verified, they are at least verified.
            // The status enum: PENDING_VERIFICATION, PROFILE_COMPLETED,
            // SUBSCRIPTION_SELECTED, VERIFIED
            // If they verify email early (before step 2), status is VERIFIED? no, VERIFIED
            // means fully done?
            // "Back passes user to VERIFIED as soon as he clicks on the activation email"
            // implying this is the final gate.
            // So let's set it to VERIFIED if they are not already.
            user.setRegistrationStatus(com.immopilot.modules.users.domain.RegistrationStatus.VERIFIED);
        }
        userRepository.save(user);
    }

    public AuthDto.AuthenticationResponse registerStep2(AuthDto.RegistrationStep2Request request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setProfileType(request.getProfileType());
        // user.setSiret(request.getSiret()); // If we had these fields
        user.setRegistrationStatus(com.immopilot.modules.users.domain.RegistrationStatus.PROFILE_COMPLETED);
        userRepository.save(user);

        return AuthDto.AuthenticationResponse.builder()
                .token(null) // Still no login
                .roles(new String[]{})
                .build();
    }

    public AuthDto.AuthenticationResponse registerStep3(AuthDto.RegistrationStep3Request request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mock subscription logic
        // if planId > 0 (paid), check paymentToken
        // Create UserSubscription...

        user.setRegistrationStatus(com.immopilot.modules.users.domain.RegistrationStatus.SUBSCRIPTION_SELECTED);
        userRepository.save(user);

        // If email verified, maybe return login token?
        if (user.isVerified()) {
            return generateTokenResponse(user);
        }

        return AuthDto.AuthenticationResponse.builder()
                .token(null)
                .roles(new String[]{})
                .build();
    }

    public AuthDto.AuthenticationResponse authenticate(AuthDto.AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow();

            if (!user.isVerified()) {
                throw new RuntimeException("User not verified");
            }

            return generateTokenResponse(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private AuthDto.AuthenticationResponse generateTokenResponse(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles());
        claims.put("profile", user.getProfileType());

        var jwtToken = jwtService.generateToken(user.getEmail(), claims);
        return AuthDto.AuthenticationResponse.builder()
                .token(jwtToken)
                .roles(user.getRoles().toArray(new String[0]))
                .menu(generateMenu(user))
                .build();
    }

    private java.util.List<com.immopilot.modules.users.service.dto.MenuItem> generateMenu(User user) {
        var menu = new java.util.ArrayList<com.immopilot.modules.users.service.dto.MenuItem>();

        // Common
        menu.add(
                new com.immopilot.modules.users.service.dto.MenuItem("Dashboard", "/app/dashboard",
                        "dashboard", true));

        // Free & up
        menu.add(new com.immopilot.modules.users.service.dto.MenuItem("My Properties", "/app/properties",
                "house",
                true));

        // Check limits or roles for advanced features
        boolean isPro = user.getRoles().contains("ROLE_USER_PRO")
                || user.getRoles().contains("ROLE_USER_STANDARD");

        // Construction is integrated in Property Detail for now, removing top level
        // link or pointing to properties
        // menu.add(new com.immopilot.modules.users.service.dto.MenuItem("Construction",
        // "/app/construction", "build", isPro));

        menu.add(
                new com.immopilot.modules.users.service.dto.MenuItem("Finance", "/app/finance",
                        "attach_money", isPro));

        if (user.getRoles().contains("ROLE_USER_PRO")) {
            menu.add(new com.immopilot.modules.users.service.dto.MenuItem("Team", "/app/users", "group",
                    true));
        }

        menu.add(new com.immopilot.modules.users.service.dto.MenuItem("Demandes", "/app/requests", "inbox",
                isPro));

        menu.add(new com.immopilot.modules.users.service.dto.MenuItem("Profile", "/app/profile", "person",
                true));
        menu.add(new com.immopilot.modules.users.service.dto.MenuItem("Settings", "/app/settings", "settings",
                true));

        return menu;
    }
}
