package ru.halcyon.meetingease.service.auth.client;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.dto.ClientRegisterDto;
import ru.halcyon.meetingease.exception.ResourceAlreadyExistsException;
import ru.halcyon.meetingease.exception.TokenValidationException;
import ru.halcyon.meetingease.exception.WrongDataException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.support.Role;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.auth.JwtProvider;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.service.mail.MailService;

@Service
@RequiredArgsConstructor
public class ClientAuthServiceImpl implements ClientAuthService {
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final ClientService clientService;
    private final MailService mailService;

    @Override
    public AuthResponse register(ClientRegisterDto dto) {
        if (clientService.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("Client with this email already exists.");
        }

        Client client = Client.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .position(dto.getPosition())
                .role(Role.USER)
                .isVerified(false)
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        clientService.save(client);
        AuthResponse response = getAuthResponse(client);
        mailService.sendSimpleVerificationMailMessage(client.getName(), client.getEmail(), response.getAccessToken());

        return response;
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        Client client = clientService.findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), client.getPassword())) {
            throw new WrongDataException("Wrong data.");
        }

        return getAuthResponse(client);
    }

    private AuthResponse getAuthResponse(Client client) {
        String accessToken = jwtProvider.generateTokenForClient(client, false);
        String refreshToken = jwtProvider.generateTokenForClient(client, true);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse getTokensByRefresh(String refreshToken, boolean isRefresh) {
        if (!jwtProvider.isValidRefreshToken(refreshToken)) {
            throw new TokenValidationException("Refresh token is not valid.");
        }

        String subject = jwtProvider.extractRefreshClaims(refreshToken).getSubject();
        Client client = clientService.findByEmail(subject);

        String accessToken = jwtProvider.generateTokenForClient(client, false);
        String newRefreshToken = isRefresh ? jwtProvider.generateTokenForClient(client, true) : null;

        return new AuthResponse(accessToken, newRefreshToken);
    }

    @Override
    public String verifyByToken(String token) {
        String subject = jwtProvider.extractAccessClaims(token).getSubject();
        Client client = clientService.findByEmail(subject);

        client.setIsVerified(true);
        clientService.save(client);

        return "Account is verified";
    }

    @Override
    public JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }
}
