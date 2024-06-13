package ru.halcyon.meetingease.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.halcyon.meetingease.dto.CompanyCreateDto;
import ru.halcyon.meetingease.exception.ResourceNotFoundException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Company;
import ru.halcyon.meetingease.repository.MeetingRepository;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.service.company.CompanyService;
import ru.halcyon.meetingease.support.Role;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.repository.CompanyRepository;
import ru.halcyon.meetingease.security.JwtAuthentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompanyServiceTests {
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private CompanyService companyService;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.6");

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

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
        meetingRepository.deleteAll();
        clientRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void findById_ReturnsCorrectCompany() {
        Company createdCompany = new Company("test_name", "test_description");
        createdCompany = companyRepository.save(createdCompany);

        Company receivedCompany = companyService.findById(createdCompany.getId());
        assertThat(receivedCompany)
                .isNotNull()
                .isEqualTo(createdCompany);
    }

    @Test
    void findById_ReturnsCorrectException() {
        assertThrows(ResourceNotFoundException.class, () -> companyService.findById(2L));
    }

    @Test
    void createCompany() {
        CompanyCreateDto dto = new CompanyCreateDto("test_name", "test_description");
        Client client = createClient("test_email@gmail.com");

        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, client.getEmail(), true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        Company createdCompany = companyService.create(dto);

        assertThat(createdCompany).isNotNull();
        assertThat(createdCompany.getName()).isEqualTo(dto.getName());
        assertThat(createdCompany.getDescription()).isEqualTo(dto.getDescription());
    }

    @Test
    void addClient() {
        addOwnerToAuth();

        Company company = companyService.create(getCompanyDto());
        Client client = createClient("test_email@gmail.com");

        company = companyService.addClient(company.getId(), client.getEmail());
        client = clientService.findById(client.getId());

        assertThat(company.getClients()).contains(client);
        assertThat(client.getCompany()).isEqualTo(company);
        assertThat(client.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void removeClient() {
        addOwnerToAuth();

        Company company = companyService.create(getCompanyDto());
        Client client = createClient("test_email@gmail.com");
        company = companyService.addClient(company.getId(), client.getEmail());

        company = companyService.removeClient(company.getId(), client.getEmail());
        client = clientService.findById(client.getId());

        assertThat(company.getClients()).doesNotContain(client);
        assertThat(client.getCompany()).isNull();
    }

    @Test
    void deleteCompany() {
        Client owner = addOwnerToAuth();
        Company company = companyService.create(getCompanyDto());

        companyService.delete(company.getId());

        assertThrows(ResourceNotFoundException.class, () -> companyService.findById(company.getId()));
        assertThat(owner.getCompany()).isNull();
    }

    @Test
    void updateDescription() {
        addOwnerToAuth();
        Company company = companyService.create(getCompanyDto());
        String newDescription = "new_test_description";

        company = companyService.updateDescription(company.getId(), newDescription);

        assertThat(company.getDescription()).isEqualTo(newDescription);
    }

    private Client addOwnerToAuth() {
        Client owner = createClient("test_owner_email@gmail.com");

        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, owner.getEmail(), true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        return owner;
    }

    private Client createClient(String email) {
        return clientRepository.save(
                Client.builder()
                        .email(email)
                        .name("test_name")
                        .surname("test_surname")
                        .password("test_password")
                        .position("test_position")
                        .phoneNumber("test_number")
                        .isVerified(true)
                        .role(Role.ADMIN)
                        .photo("test_photo")
                        .build()
        );
    }

    private CompanyCreateDto getCompanyDto() {
        return new CompanyCreateDto("test_name", "test_description");
    }
}
