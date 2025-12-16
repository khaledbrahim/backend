package com.immopilot.modules.users.web;

import com.immopilot.modules.users.service.AuthService;
import com.immopilot.modules.users.service.dto.AuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/step1")
    public ResponseEntity<AuthDto.AuthenticationResponse> registerStep1(
            @RequestBody AuthDto.RegistrationStep1Request request) {
        return ResponseEntity.ok(authService.registerStep1(request));
    }

    // Endpoints for Step 2 and 3
    @PostMapping("/register/step2")
    public ResponseEntity<AuthDto.AuthenticationResponse> registerStep2(
            @RequestBody AuthDto.RegistrationStep2Request request) {
        return ResponseEntity.ok(authService.registerStep2(request));
    }

    @PostMapping("/register/step3")
    public ResponseEntity<AuthDto.AuthenticationResponse> registerStep3(
            @RequestBody AuthDto.RegistrationStep3Request request) {
        return ResponseEntity.ok(authService.registerStep3(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthenticationResponse> authenticate(
            @RequestBody AuthDto.AuthenticationRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
