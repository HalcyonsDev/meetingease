package ru.halcyon.meetingease.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.halcyon.meetingease.config.PublicEndpoint;
import ru.halcyon.meetingease.exception.TokenVerificationException;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.RefreshTokenHeaderProvider;
import ru.halcyon.meetingease.service.auth.AgentAuthService;

@RestController
@RequestMapping("/api/v1/agents/auth")
@RequiredArgsConstructor
public class AgentAuthController {
    private final RefreshTokenHeaderProvider refreshTokenHeaderProvider;
    private final AgentAuthService agentAuthService;

    @PublicEndpoint
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = agentAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    @PublicEndpoint
    @PutMapping(value = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> getAccessToken() {
        String refreshToken = refreshTokenHeaderProvider.getRefreshToken()
                .orElseThrow(TokenVerificationException::new);
        AuthResponse response = agentAuthService.getTokensByRefresh(refreshToken, false);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE    )
    public ResponseEntity<AuthResponse> getRefreshToken() {
        String refreshToken = refreshTokenHeaderProvider.getRefreshToken()
                .orElseThrow(TokenVerificationException::new);
        AuthResponse response = agentAuthService.getTokensByRefresh(refreshToken, true);
        return ResponseEntity.ok(response);
    }
}
