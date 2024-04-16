package ru.halcyon.meetingease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.halcyon.meetingease.dto.AgentRegisterDto;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.RefreshRequest;
import ru.halcyon.meetingease.service.auth.agent.AgentAuthService;

@RestController
@RequestMapping("/api/v1/agents/auth")
@RequiredArgsConstructor
public class AgentAuthController {
    private final AgentAuthService agentAuthService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = agentAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/access")
    public ResponseEntity<AuthResponse> getAccessToken(@RequestBody RefreshRequest request) {
        AuthResponse response = agentAuthService.getTokensByRefresh(request.getRefreshToken(), false);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> getRefreshToken(@RequestBody RefreshRequest request) {
        AuthResponse response = agentAuthService.getTokensByRefresh(request.getRefreshToken(), true);
        return ResponseEntity.ok(response);
    }
}
