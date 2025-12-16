package com.immopilot.modules.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.immopilot.modules.users.service.dto.AuthDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@AutoConfigureMockMvc
@org.testcontainers.junit.jupiter.Testcontainers
class AuthIntegrationTest {

    @org.testcontainers.junit.jupiter.Container
    @org.springframework.boot.testcontainers.service.connection.ServiceConnection
    static org.testcontainers.containers.PostgreSQLContainer<?> postgres = new org.testcontainers.containers.PostgreSQLContainer<>(
            "postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.immopilot.modules.users.repository.UserRepository userRepository;

    @Test
    void shouldCompleteFullRegistrationFlow() throws Exception {
        String email = "john.full@example.com";
        // 1. Register Step 1
        var step1 = AuthDto.RegistrationStep1Request.builder()
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .password("password123")
                .phone("1234567890")
                .country("France")
                .build();

        mockMvc.perform(post("/api/auth/register/step1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step1)))
                .andExpect(status().isOk());

        // 2. Verify Email
        var user = userRepository.findByEmail(email).orElseThrow();
        String token = user.getActivationToken();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/auth/verify-email")
                        .param("token", token))
                .andExpect(status().isOk());

        // 3. Register Step 2
        var step2 = AuthDto.RegistrationStep2Request.builder()
                .email(email)
                .profileType(com.immopilot.modules.users.domain.ProfileType.INVESTOR_PRIVATE)
                .build();

        mockMvc.perform(post("/api/auth/register/step2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step2)))
                .andExpect(status().isOk());

        // 4. Register Step 3 (Select Free Plan for example)
        var step3 = AuthDto.RegistrationStep3Request.builder()
                .email(email)
                .planId(1L) // dummy
                .paymentToken("dummy-pay-token")
                .build();

        mockMvc.perform(post("/api/auth/register/step3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(step3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists()); // Should return token now if verified

        // 5. Login
        var loginRequest = AuthDto.AuthenticationRequest.builder()
                .email(email)
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
