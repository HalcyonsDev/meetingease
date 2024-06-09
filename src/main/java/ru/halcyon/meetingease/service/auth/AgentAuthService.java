package ru.halcyon.meetingease.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.config.TokenConfigProperties;
import ru.halcyon.meetingease.exception.TokenVerificationException;
import ru.halcyon.meetingease.exception.InvalidCredentialsException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.security.*;
import ru.halcyon.meetingease.service.agent.AgentService;
import ru.halcyon.meetingease.util.CacheManager;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AgentAuthService {
    private final JwtProvider jwtProvider;
    private final CacheManager cacheManager;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final TokenConfigProperties tokenConfigProperties;
    private final PasswordEncoder passwordEncoder;
    private final AgentService agentService;

    public AuthResponse login(AuthRequest request) {
        Agent agent = agentService.findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), agent.getPassword())) {
            throw new InvalidCredentialsException("Invalid login credentials provided.");
        }

        AuthResponse response = getAuthResponse(agent);
        int refreshTokenValidity = tokenConfigProperties.getRefreshToken().getValidity();
        cacheManager.save(response.getRefreshToken(), agent.getEmail(), Duration.ofMinutes(refreshTokenValidity));

        return response;
    }

    public AuthResponse getTokensByRefresh(String refreshToken, boolean isRefresh) {
        String subject = cacheManager.fetch(refreshToken, String.class)
                .orElseThrow(TokenVerificationException::new);
        Agent agent = agentService.findByEmail(subject);

        String accessToken = jwtProvider.generateAccessTokenForAgent(agent);
        String newRefreshToken = isRefresh ? refreshTokenGenerator.generate() : null;

        return new AuthResponse(accessToken, newRefreshToken);
    }

    private AuthResponse getAuthResponse(Agent agent) {
        String accessToken = jwtProvider.generateAccessTokenForAgent(agent);
        String refreshToken = refreshTokenGenerator.generate();

        return new AuthResponse(accessToken, refreshToken);
    }

    public JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }
}
