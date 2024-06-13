package ru.halcyon.meetingease.service;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.halcyon.meetingease.exception.InvalidCredentialsException;
import ru.halcyon.meetingease.exception.TokenVerificationException;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.security.JwtProvider;
import ru.halcyon.meetingease.service.auth.AgentAuthService;
import ru.halcyon.meetingease.util.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AgentAuthServiceTests {
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.6");

    @Container
    private static final RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:5.0.5-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private CacheManager cacheManager;

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
        assertThat(redis.isCreated()).isTrue();
        assertThat(redis.isRunning()).isTrue();
    }

    @Test
    void login() {
        AuthResponse response = agentAuthService.login(getAuthRequest());
        isValidAuthResponse(response);
    }

    @Test
    void loginChecksPassword() {
        InvalidCredentialsException wrongDataException = assertThrows(InvalidCredentialsException.class,
                () -> agentAuthService.login(new AuthRequest(EMAIL, "wrong_password")));
        assertThat(wrongDataException.getMessage()).isEqualTo("Invalid login credentials provided.");
    }

    @Test
    void getTokensByRefreshByIsNotRefresh() {
        String refreshToken = agentAuthService.login(getAuthRequest()).getRefreshToken();
        AuthResponse response = agentAuthService.getTokensByRefresh(refreshToken, false);

        isValidAuthResponse(response);
    }

    @Test
    void getTokensByRefreshByIsRefresh() {
        String refreshToken = agentAuthService.login(getAuthRequest()).getRefreshToken();
        AuthResponse response = agentAuthService.getTokensByRefresh(refreshToken, true);

        isValidAuthResponse(response);
        isValidRefreshToken(response.getRefreshToken(), EMAIL);
    }

    @Test
    void getTokensByRefreshChecksToken() {
        TokenVerificationException tokenValidationException = assertThrows(TokenVerificationException.class,
                () -> agentAuthService.getTokensByRefresh("invalid_token", false));
        assertThat(tokenValidationException.getMessage()).isEqualTo("Authentication failure: Token missing, invalid, revoked or expired.");
    }

    private AuthRequest getAuthRequest() {
        return new AuthRequest(EMAIL, "TestPassword123");
    }

    private void isValidAuthResponse(AuthResponse response) {
        assertThat(response).isNotNull();
        isValidAccessToken(response.getAccessToken());
    }

    private void isValidAccessToken(String accessToken) {
        assertNotNull(accessToken);
        assertTrue(jwtProvider.isValidAccessToken(accessToken));
    }

    private void isValidRefreshToken(String refreshToken, String email) {
        assertTrue(cacheManager.isPresent(refreshToken));
        assertThat(cacheManager.fetch(refreshToken, String.class)).contains(email);
    }
}
