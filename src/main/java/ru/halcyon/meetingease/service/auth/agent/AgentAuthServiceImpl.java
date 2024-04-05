package ru.halcyon.meetingease.service.auth.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.dto.AgentRegisterDto;
import ru.halcyon.meetingease.exception.ResourceAlreadyExistsException;
import ru.halcyon.meetingease.exception.TokenValidationException;
import ru.halcyon.meetingease.exception.WrongDataException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.service.agent.AgentService;
import ru.halcyon.meetingease.service.auth.JwtProvider;

@Service
@RequiredArgsConstructor
public class AgentAuthServiceImpl implements AgentAuthService {
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AgentService agentService;

    @Override
    public AuthResponse register(AgentRegisterDto dto) {
        if (agentService.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("Agent with this email already exists.");
        }

        Agent agent = Agent.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        agentService.save(agent);

        return getAuthResponse(agent);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        Agent agent = agentService.findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), agent.getPassword())) {
            throw new WrongDataException("Wrong data.");
        }

        return getAuthResponse(agent);
    }

    @Override
    public AuthResponse getTokensByRefresh(String refreshToken, boolean isRefresh) {
        if (!jwtProvider.isValidRefreshToken(refreshToken)) {
            throw new TokenValidationException("Refresh token is not valid.");
        }

        String subject = jwtProvider.extractRefreshToken(refreshToken).getSubject();
        Agent agent = agentService.findByEmail(subject);

        String accessToken = jwtProvider.generateTokenForAgent(agent, false);
        String newRefreshToken = isRefresh ? jwtProvider.generateTokenForAgent(agent, true) : null;

        return new AuthResponse(accessToken, newRefreshToken);
    }

    private AuthResponse getAuthResponse(Agent agent) {
        String accessToken = jwtProvider.generateTokenForClient(agent, false);
        String refreshToken = jwtProvider.generateTokenForClient(agent, true);

        return new AuthResponse(accessToken, refreshToken);
    }
}
