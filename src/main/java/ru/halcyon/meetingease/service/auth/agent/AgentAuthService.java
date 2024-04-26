package ru.halcyon.meetingease.service.auth.agent;

import ru.halcyon.meetingease.dto.AgentRegisterDto;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.JwtAuthentication;

public interface AgentAuthService {
    AuthResponse login(AuthRequest request);
    AuthResponse getTokensByRefresh(String refreshToken, boolean isRefresh);
    JwtAuthentication getAuthInfo();
}
