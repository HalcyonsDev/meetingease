package ru.halcyon.meetingease.service.auth;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.halcyon.meetingease.dto.ClientRegisterDto;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.service.auth.client.ClientAuthServiceImpl;
import ru.halcyon.meetingease.service.client.ClientServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientAuthServiceTests {
    @Mock
    private ClientServiceImpl clientService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private ClientAuthServiceImpl clientAuthService;

    private static Client client;

    @BeforeAll
    static void beforeAll() {
        client = Client.builder()
                .name("test_name")
                .surname("test_surname")
                .email("test_email@gmail.com")
                .phoneNumber("+8920394233")
                .password("test_Password123")
                .position("test_position")
                .build();
    }

    @Test
    void RegisterMethod_ReturnsAuthResponse() {
        ClientRegisterDto dto = new ClientRegisterDto(
            client.getName(),
            client.getSurname(),
            client.getEmail(),
            client.getPhoneNumber(),
            client.getPosition(),
            client.getPassword()
        );

        when(clientService.save(Mockito.any(Client.class))).thenReturn(client);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("HASHED_PASSWORD");
        when(jwtProvider.generateTokenForClient(Mockito.any(Client.class), Mockito.anyBoolean())).thenReturn("TOKEN.TOKEN.TOKEN");

        AuthResponse response = clientAuthService.register(dto);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertThat(response.getTYPE()).isEqualTo("Bearer");
    }

    @Test
    void LoginMethod_ReturnsAuthResponse() {
        AuthRequest request = new AuthRequest(client.getEmail(), client.getPassword());

        when(clientService.findByEmail(client.getEmail())).thenReturn(client);
        when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        when(jwtProvider.generateTokenForClient(Mockito.any(Client.class), Mockito.anyBoolean())).thenReturn("TOKEN.TOKEN.TOKEN");

        AuthResponse response = clientAuthService.login(request);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertThat(response.getTYPE()).isEqualTo("Bearer");
    }
}
