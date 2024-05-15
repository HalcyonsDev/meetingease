package ru.halcyon.meetingease.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.halcyon.meetingease.TestPostgresContainer;
import ru.halcyon.meetingease.exception.TokenValidationException;
import ru.halcyon.meetingease.exception.WrongDataException;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.auth.JwtProvider;
import ru.halcyon.meetingease.service.auth.agent.AgentAuthService;
import ru.halcyon.meetingease.util.JwtUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AgentAuthServiceTests {
    @Container
    static PostgreSQLContainer<?> postgres = TestPostgresContainer.getInstance();

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private AgentAuthService agentAuthService;

    private static final String EMAIL = "ivan.ivanov@example.com";

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @Test
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void login() {
        AuthResponse response = agentAuthService.login(getAuthRequest());
        isValidAuthResponse(response);
    }

    @Test
    void loginChecksPassword() {
        WrongDataException wrongDataException = assertThrows(WrongDataException.class,
                () -> agentAuthService.login(new AuthRequest(EMAIL, "wrong_password")));
        assertThat(wrongDataException.getMessage()).isEqualTo("Wrong data.");
    }

    @Test
    void getTokensByRefreshByIsNotRefresh() {
        String refreshToken = agentAuthService.login(getAuthRequest()).getRefreshToken();
        AuthResponse response = agentAuthService.getTokensByRefresh(refreshToken, false);

        assertThat(response).isNotNull();
        assertThat(response.getRefreshToken()).isNull();
        isValidAccessToken(response.getAccessToken());
    }

    @Test
    void getTokensByRefreshByIsRefresh() {
        String refreshToken = agentAuthService.login(getAuthRequest()).getRefreshToken();
        AuthResponse response = agentAuthService.getTokensByRefresh(refreshToken, true);

        isValidAuthResponse(response);
    }

    @Test
    void getTokensByRefreshChecksToken() {
        TokenValidationException tokenValidationException = assertThrows(TokenValidationException.class,
                () -> agentAuthService.getTokensByRefresh("invalid_token", false));
        assertThat(tokenValidationException.getMessage()).isEqualTo("Refresh token is not valid.");
    }

    @Test
    void getAuthInfo() {
        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, EMAIL, false);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        isValidJwtAuth(agentAuthService.getAuthInfo());
    }

    private AuthRequest getAuthRequest() {
        return new AuthRequest(EMAIL, "TestPassword123");
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
        assertThat(jwtAuthInfo.isClient()).isFalse();
    }
}
