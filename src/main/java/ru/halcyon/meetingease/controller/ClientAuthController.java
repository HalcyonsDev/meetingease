package ru.halcyon.meetingease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.halcyon.meetingease.config.PublicEndpoint;
import ru.halcyon.meetingease.dto.ClientRegisterDto;
import ru.halcyon.meetingease.exception.TokenVerificationException;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.RefreshTokenHeaderProvider;
import ru.halcyon.meetingease.service.auth.ClientAuthService;

@RestController
@RequestMapping("/api/v1/clients/auth")
@RequiredArgsConstructor
public class ClientAuthController {
    private final RefreshTokenHeaderProvider refreshTokenHeaderProvider;
    private final ClientAuthService clientAuthService;

    @PublicEndpoint
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid ClientRegisterDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        AuthResponse response = clientAuthService.register(dto);
        return ResponseEntity.ok(response);
    }

    @PublicEndpoint
    @PostMapping
    public ResponseEntity<String> confirmEmail(@RequestParam String token) {
        String response = clientAuthService.verifyByToken(token);
        return ResponseEntity.ok(response);
    }

    @PublicEndpoint
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = clientAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    @PublicEndpoint
    @PutMapping(value = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> getAccessToken() {
        String refreshToken = refreshTokenHeaderProvider.getRefreshToken()
                .orElseThrow(TokenVerificationException::new);
        AuthResponse response = clientAuthService.getTokensByRefresh(refreshToken, false);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> getRefreshToken() {
        String refreshToken = refreshTokenHeaderProvider.getRefreshToken()
                .orElseThrow(TokenVerificationException::new);
        AuthResponse response = clientAuthService.getTokensByRefresh(refreshToken, true);
        return ResponseEntity.ok(response);
    }
}
