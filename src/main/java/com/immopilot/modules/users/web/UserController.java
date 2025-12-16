package com.immopilot.modules.users.web;

import com.immopilot.modules.users.service.UserService;
import com.immopilot.modules.users.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDto.UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }

    @PutMapping
    public ResponseEntity<UserDto.UserProfileResponse> updateProfile(
            @RequestBody UserDto.UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @PutMapping("/settings")
    public ResponseEntity<UserDto.UserProfileResponse> updateSettings(
            @RequestBody UserDto.UpdateSettingsRequest request) {
        return ResponseEntity.ok(userService.updateSettings(request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody UserDto.ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/email")
    public ResponseEntity<Void> requestEmailChange(@RequestBody UserDto.ChangeEmailRequest request) {
        userService.requestEmailChange(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok().build();
    }
}
