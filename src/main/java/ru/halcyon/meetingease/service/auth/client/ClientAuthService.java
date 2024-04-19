package ru.halcyon.meetingease.service.auth.client;

import ru.halcyon.meetingease.dto.ClientRegisterDto;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.JwtAuthentication;

public interface ClientAuthService {
    AuthResponse register(ClientRegisterDto dto);
    AuthResponse login(AuthRequest request);
    AuthResponse getTokensByRefresh(String refreshToken, boolean isRefresh);
    String verifyByToken(String token);
    JwtAuthentication getAuthInfo();
}
