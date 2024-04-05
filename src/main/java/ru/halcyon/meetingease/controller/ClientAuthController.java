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
import ru.halcyon.meetingease.dto.ClientRegisterDto;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.RefreshRequest;
import ru.halcyon.meetingease.service.auth.client.ClientAuthService;

@RestController
@RequestMapping("/api/v1/clients/auth")
@RequiredArgsConstructor
public class ClientAuthController {
    private final ClientAuthService clientAuthService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid ClientRegisterDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        AuthResponse response = clientAuthService.register(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = clientAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/access")
    public ResponseEntity<AuthResponse> getAccessToken(@RequestBody RefreshRequest request) {
        AuthResponse response = clientAuthService.getTokensByRefresh(request.getRefreshToken(), false);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> getRefreshToken(@RequestBody RefreshRequest request) {
        AuthResponse response = clientAuthService.getTokensByRefresh(request.getRefreshToken(), true);
        return ResponseEntity.ok(response);
    }
}
