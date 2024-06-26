package ru.halcyon.meetingease.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.dto.ClientRegisterDto;
import ru.halcyon.meetingease.exception.ResourceAlreadyExistsException;
import ru.halcyon.meetingease.exception.TokenVerificationException;
import ru.halcyon.meetingease.exception.InvalidCredentialsException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.security.*;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.service.mail.MailService;
import ru.halcyon.meetingease.support.Role;
import ru.halcyon.meetingease.util.CacheManager;

@Service
@RequiredArgsConstructor
public class ClientAuthService {
    private final JwtProvider jwtProvider;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final CacheManager cacheManager;
    private final PasswordEncoder passwordEncoder;
    private final ClientService clientService;
    private final MailService mailService;

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

    
    public AuthResponse login(AuthRequest request) {
        Client client = clientService.findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), client.getPassword())) {
            throw new InvalidCredentialsException("Invalid login credentials provided.");
        }

        return getAuthResponse(client);
    }

    private AuthResponse getAuthResponse(Client client) {
        String accessToken = jwtProvider.generateAccessTokenForClient(client);
        String refreshToken = refreshTokenGenerator.generate(client.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }

    
    public AuthResponse getTokensByRefresh(String refreshToken, boolean isRefresh) {
        String subject = cacheManager.fetch(refreshToken, String.class)
                .orElseThrow(TokenVerificationException::new);
        Client client = clientService.findByEmail(subject);

        String accessToken = jwtProvider.generateAccessTokenForClient(client);
        String newRefreshToken = isRefresh ? refreshTokenGenerator.generate(client.getEmail()) : null;

        return new AuthResponse(accessToken, newRefreshToken);
    }

    
    public String verifyByToken(String token) {
        String subject = jwtProvider.extractEmail(token);
        Client client = clientService.findByEmail(subject);

        client.setIsVerified(true);
        clientService.save(client);

        return "Account is verified";
    }
}
