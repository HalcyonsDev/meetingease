package ru.halcyon.meetingease.service;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.halcyon.meetingease.dto.ClientRegisterDto;
import ru.halcyon.meetingease.exception.ResourceAlreadyExistsException;
import ru.halcyon.meetingease.exception.InvalidCredentialsException;
import ru.halcyon.meetingease.exception.TokenVerificationException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.AuthResponse;
import ru.halcyon.meetingease.security.JwtProvider;
import ru.halcyon.meetingease.service.auth.ClientAuthService;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.util.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClientAuthServiceTests {
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientAuthService clientAuthService;

    @Autowired
    private CacheManager cacheManager;

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
        assertThat(redis.isCreated()).isTrue();
        assertThat(redis.isRunning()).isTrue();
    }

    @Test
    void register() {
        ClientRegisterDto dto = getClientDto();

        AuthResponse response = clientAuthService.register(dto);
        isValidAuthResponse(response);

        Client client = clientService.findByEmail(dto.getEmail());

        assertThat(client).isNotNull();
        assertThat(client.getName()).isEqualTo(dto.getName());
        assertThat(client.getSurname()).isEqualTo(dto.getSurname());
        assertThat(client.getEmail()).isEqualTo(dto.getEmail());
        assertThat(client.getPhoneNumber()).isEqualTo(dto.getPhoneNumber());
        assertThat(client.getPosition()).isEqualTo(dto.getPosition());
        assertThat(passwordEncoder.matches(dto.getPassword(), client.getPassword())).isTrue();
        isValidRefreshToken(response.getRefreshToken(), client.getEmail());
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
        isValidRefreshToken(response.getRefreshToken(), dto.getEmail());
    }

    @Test
    void loginChecksPassword() {
        ClientRegisterDto dto = getClientDto();
        clientAuthService.register(dto);

        AuthRequest request = new AuthRequest(dto.getEmail(), "test_password");
        InvalidCredentialsException wrongData = assertThrows(InvalidCredentialsException.class, () -> clientAuthService.login(request));

        assertThat(wrongData).isNotNull();
        assertThat(wrongData.getMessage()).isEqualTo("Invalid login credentials provided.");
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
        ClientRegisterDto dto = getClientDto();
        String refreshToken = clientAuthService.register(dto).getRefreshToken();
        AuthResponse response = clientAuthService.getTokensByRefresh(refreshToken, true);

        isValidAuthResponse(response);
        isValidRefreshToken(response.getRefreshToken(), dto.getEmail());
    }

    @Test
    void getTokensByRefreshChecksToken() {
        TokenVerificationException tokenValidationException = assertThrows(TokenVerificationException.class,
                () -> clientAuthService.getTokensByRefresh("invalid_token", false));
        assertThat(tokenValidationException.getMessage()).isEqualTo("Authentication failure: Token missing, invalid, revoked or expired.");
    }

    @Test
    void verifyByToken() {
        String accessToken = clientAuthService.register(getClientDto()).getAccessToken();
        String response = clientAuthService.verifyByToken(accessToken);
        Client client = clientService.findByEmail(EMAIL);

        assertThat(response).isEqualTo("Account is verified");
        assertThat(client.getIsVerified()).isTrue();
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
    }

    private void isValidAccessToken(String accessToken) {
        assertThat(accessToken).isNotNull();
        assertThat(jwtProvider.isValidAccessToken(accessToken)).isTrue();
    }

    private void isValidRefreshToken(String refreshToken, String email) {
        assertTrue(cacheManager.isPresent(refreshToken));
        assertThat(cacheManager.fetch(refreshToken, String.class)).contains(email);
    }
}
