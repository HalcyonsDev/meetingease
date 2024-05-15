package ru.halcyon.meetingease.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
import ru.halcyon.meetingease.exception.ResourceForbiddenException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.agent.AgentService;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.support.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClientServiceTests {
    @Container
    static PostgreSQLContainer<?> postgres = TestPostgresContainer.getInstance();

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private AgentService agentService;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @AfterEach
    void tearDown() {
        clientRepository.deleteAll();
    }

    @Test
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void findById() {
        Client createdClient = createClient(true);
        Client receivedClient = clientService.findById(createdClient.getId());

        assertThat(receivedClient)
                .isNotNull()
                .isEqualTo(createdClient);
    }

    @Test
    void findByEmail() {
        Client createdClient = createClient(true);
        Client receivedClient = clientService.findByEmail(createdClient.getEmail());

        assertThat(receivedClient)
                .isNotNull()
                .isEqualTo(createdClient);
    }

    @Test
    void isVerifiedClient_ChecksForClient() {
        Agent agent = agentService.findById(1L);

        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, agent.getEmail(), false);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        ResourceForbiddenException forbiddenException = assertThrows(ResourceForbiddenException.class, () -> clientService.isVerifiedClient());

        assertThat(forbiddenException.getMessage()).isEqualTo("This feature is not allowed for agents");
    }

    @Test
    void isVerifiedClient_ChecksThatClientIsVerified() {
        Client client = createClient(false);

        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, client.getEmail(), true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        ResourceForbiddenException forbiddenException = assertThrows(ResourceForbiddenException.class, () -> clientService.isVerifiedClient());

        assertThat(forbiddenException.getMessage()).isEqualTo("This feature is not allowed for unverified users. Please confirm your email.");
    }

    private Client createClient(boolean isVerified) {
        return clientRepository.save(
                Client.builder()
                        .email("test_email@gmail.com")
                        .name("test_name")
                        .surname("test_surname")
                        .password("test_password")
                        .position("test_position")
                        .phoneNumber("test_number")
                        .isVerified(isVerified)
                        .role(Role.ADMIN)
                        .photo("test_photo")
                        .build()
        );
    }
}
