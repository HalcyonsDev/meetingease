package ru.halcyon.meetingease.service.auth.agent;

import ru.halcyon.meetingease.dto.AgentRegisterDto;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;

public interface AgentAuthService {
    AuthResponse login(AuthRequest request);
    AuthResponse getTokensByRefresh(String refreshToken, boolean isRefresh);
}
