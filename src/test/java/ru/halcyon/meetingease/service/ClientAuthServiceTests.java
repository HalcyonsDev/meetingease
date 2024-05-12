package ru.halcyon.meetingease.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.halcyon.meetingease.TestPostgresContainer;
import ru.halcyon.meetingease.dto.ClientRegisterDto;
import ru.halcyon.meetingease.exception.ResourceAlreadyExistsException;
import ru.halcyon.meetingease.exception.TokenValidationException;
import ru.halcyon.meetingease.exception.WrongDataException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.auth.JwtProvider;
import ru.halcyon.meetingease.service.auth.client.ClientAuthService;
import ru.halcyon.meetingease.util.JwtUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ClientAuthServiceTests {
    @Container
    static PostgreSQLContainer<?> postgres = TestPostgresContainer.getInstance();

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ClientAuthService clientAuthService;

    private static final String EMAIL = "test_email@gmail.com";

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
    }

    @Test
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void register() {
        ClientRegisterDto dto = getClientDto();

        AuthResponse response = clientAuthService.register(dto);
        isValidAuthResponse(response);

        Client client = clientRepository.findByEmail(dto.getEmail()).get();

        assertThat(client).isNotNull();
        assertThat(client.getName()).isEqualTo(dto.getName());
        assertThat(client.getSurname()).isEqualTo(dto.getSurname());
        assertThat(client.getEmail()).isEqualTo(dto.getEmail());
        assertThat(client.getPhoneNumber()).isEqualTo(dto.getPhoneNumber());
        assertThat(client.getPosition()).isEqualTo(dto.getPosition());
        assertThat(passwordEncoder.matches(dto.getPassword(), client.getPassword())).isTrue();
    }

    @Test
    void registerChecksClientExisting() {
        clientAuthService.register(getClientDto());
        ResourceAlreadyExistsException resourceAlreadyExistsException = assertThrows(ResourceAlreadyExistsException.class,
                () -> clientAuthService.register(getClientDto()));
        assertThat(resourceAlreadyExistsException.getMessage()).isEqualTo("Client with this email already exists.");
    }

    @Test
    void login() {
        ClientRegisterDto dto = getClientDto();
        clientAuthService.register(dto);

        AuthRequest request = new AuthRequest(dto.getEmail(), dto.getPassword());
        AuthResponse response = clientAuthService.login(request);

        isValidAuthResponse(response);
    }

    @Test
    void loginChecksPassword() {
        ClientRegisterDto dto = getClientDto();
        clientAuthService.register(dto);

        AuthRequest request = new AuthRequest(dto.getEmail(), "test_password");
        WrongDataException wrongData = assertThrows(WrongDataException.class, () -> clientAuthService.login(request));

        assertThat(wrongData).isNotNull();
        assertThat(wrongData.getMessage()).isEqualTo("Wrong data.");
    }

    @Test
    void getTokensByRefreshByIsNotRefresh() {
        String refreshToken = clientAuthService.register(getClientDto()).getRefreshToken();
        AuthResponse response = clientAuthService.getTokensByRefresh(refreshToken, false);

        assertThat(response).isNotNull();
        assertThat(response.getRefreshToken()).isNull();
        isValidAccessToken(response.getAccessToken());
    }

    @Test
    void getTokensByRefreshByIsRefresh() {
        String refreshToken = clientAuthService.register(getClientDto()).getRefreshToken();
        AuthResponse response = clientAuthService.getTokensByRefresh(refreshToken, true);

        isValidAuthResponse(response);
    }

    @Test
    void getTokensByRefreshChecksToken() {
        TokenValidationException tokenValidationException = assertThrows(TokenValidationException.class,
                () -> clientAuthService.getTokensByRefresh("invalid_token", false));
        assertThat(tokenValidationException.getMessage()).isEqualTo("Refresh token is not valid.");
    }

    @Test
    void verifyByToken() {
        String accessToken = clientAuthService.register(getClientDto()).getAccessToken();
        String response = clientAuthService.verifyByToken(accessToken);
        Client client = clientRepository.findByEmail(EMAIL).get();

        assertThat(response).isEqualTo("Account is verified");
        assertThat(client.getIsVerified()).isTrue();
    }

    @Test
    void getAuthInfo() {
        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, EMAIL, true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        isValidJwtAuth(clientAuthService.getAuthInfo());
    }

    private ClientRegisterDto getClientDto() {
        return new ClientRegisterDto(
                "test_name",
                "test_surname",
                EMAIL,
                "+77777777",
                "test_position",
                "TestPassword123"
        );
    }

    private void isValidAuthResponse(AuthResponse response) {
        assertThat(response).isNotNull();
        isValidAccessToken(response.getAccessToken());
        isValidRefreshToken(response.getRefreshToken());
    }

    private void isValidAccessToken(String accessToken) {
        assertThat(accessToken).isNotNull();
        assertThat(jwtProvider.isValidAccessToken(accessToken)).isTrue();

        Claims claims = jwtProvider.extractAccessClaims(accessToken);
        JwtAuthentication jwtAuthInfo = JwtUtil.getAuthentication(claims);

        isValidJwtAuth(jwtAuthInfo);
    }

    private void isValidRefreshToken(String refreshToken) {
        assertThat(refreshToken).isNotNull();
        assertThat(jwtProvider.isValidRefreshToken(refreshToken)).isTrue();

        Claims claims = jwtProvider.extractRefreshClaims(refreshToken);
        JwtAuthentication jwtAuthInfo = JwtUtil.getAuthentication(claims);

        isValidJwtAuth(jwtAuthInfo);
    }

    private void isValidJwtAuth(JwtAuthentication jwtAuthInfo) {
        assertThat(jwtAuthInfo.getEmail()).isEqualTo(EMAIL);
        assertThat(jwtAuthInfo.isClient()).isTrue();
    }
}
