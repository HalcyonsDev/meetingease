package ru.halcyon.meetingease.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.halcyon.meetingease.TestPostgresContainer;
import ru.halcyon.meetingease.dto.ClientUpdateDto;
import ru.halcyon.meetingease.dto.CompanyCreateDto;
import ru.halcyon.meetingease.exception.ResourceForbiddenException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Company;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.repository.CompanyRepository;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.agent.AgentService;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.service.company.CompanyService;
import ru.halcyon.meetingease.support.Role;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClientServiceTests {
    @Container
    static PostgreSQLContainer<?> postgres = TestPostgresContainer.getInstance();

    private static final String ADMIN_EMAIL = "test_email@gmail.com";

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyService companyService;

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

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void findById() {
        Client createdClient = createClient(true, ADMIN_EMAIL);
        Client receivedClient = clientService.findById(createdClient.getId());

        assertThat(receivedClient)
                .isNotNull()
                .isEqualTo(createdClient);
    }

    @Test
    void findByEmail() {
        Client createdClient = createClient(true, ADMIN_EMAIL);
        Client receivedClient = clientService.findByEmail(createdClient.getEmail());

        assertThat(receivedClient)
                .isNotNull()
                .isEqualTo(createdClient);
    }

    @Test
    void isVerifiedClient_ChecksForClient() {
        Agent agent = agentService.findById(1L);
        setJwtAuth(false, agent.getEmail());

        ResourceForbiddenException forbiddenException = assertThrows(ResourceForbiddenException.class, () -> clientService.isVerifiedClient());

        assertThat(forbiddenException.getMessage()).isEqualTo("This feature is not allowed for agents");
    }

    @Test
    void isVerifiedClient_ChecksThatClientIsVerified() {
        createClient(false, ADMIN_EMAIL);
        setJwtAuth(true, ADMIN_EMAIL);

        ResourceForbiddenException forbiddenException = assertThrows(ResourceForbiddenException.class, () -> clientService.isVerifiedClient());

        assertThat(forbiddenException.getMessage()).isEqualTo("This feature is not allowed for unverified users. Please confirm your email.");
    }

    @Test
    void uploadPhoto() {
        createClient(true, ADMIN_EMAIL);
        setJwtAuth(true, ADMIN_EMAIL);

        MockMultipartFile mockFile = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "dummy image content".getBytes());
        Client client = clientService.uploadPhoto(mockFile);

        assertThat(client).isNotNull();
        assertTrue(Files.exists(Path.of(client.getPhoto())));
    }

    @Test
    void updateData() {
        createClient(true, ADMIN_EMAIL);
        createClient(true, "some_email@gmail.com");

        ClientUpdateDto dto = new ClientUpdateDto(
                "new_name",
                "",
                "new_email@gmail.com",
                "",
                "new_position"
        );


        setJwtAuth(true, ADMIN_EMAIL);
        Client client = clientService.updateData(dto);

        assertThat(client).isNotNull();
        assertThat(client.getName()).isEqualTo(dto.getName());
        assertThat(client.getSurname()).isEqualTo("test_surname");
        assertThat(client.getEmail()).isEqualTo("new_email@gmail.com");
        assertThat(client.getIsVerified()).isFalse();
        assertThat(client.getPosition()).isEqualTo(dto.getPosition());
    }

    @Test
    void updateRole() {
        createClient(true, ADMIN_EMAIL);
        Company company = createCompany();
        Client client = createClient(true, "some_email@gmail.com");
        companyService.addClient(company.getId(), client.getEmail());

        setJwtAuth(true, ADMIN_EMAIL);
        client = clientService.updateRole(client.getId(), Role.USER);

        assertThat(client).isNotNull();
        assertThat(client.getRole()).isEqualTo(Role.USER);
    }

    private Client createClient(boolean isVerified, String email) {
        return clientRepository.save(
                Client.builder()
                        .email(email)
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

    private Company createCompany() {
        setJwtAuth(true, ADMIN_EMAIL);
        return companyService.create(
                new CompanyCreateDto("test_name", "test_description")
        );
    }

    private void setJwtAuth(boolean isClient, String email) {
        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, email, isClient);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
    }
}
