package ru.halcyon.meetingease.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import ru.halcyon.meetingease.dto.MeetingCreateDto;
import ru.halcyon.meetingease.exception.ResourceNotFoundException;
import ru.halcyon.meetingease.exception.WrongDataException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Meeting;
import ru.halcyon.meetingease.model.support.Role;
import ru.halcyon.meetingease.repository.AgentRepository;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.repository.DealRepository;
import ru.halcyon.meetingease.repository.MeetingRepository;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.agent.AgentService;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.service.meeting.MeetingService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MeetingServiceTests {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private AgentService agentService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
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

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        meetingRepository.deleteAll();
    }

    @Test
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void createMeeting_ChooseFreeAgent() {
        Client client = createClient("test_email@gmail.com");

        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, client.getEmail(), true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        meetingService.create(
                new MeetingCreateDto(getDate(2, 9, 30), "казань бауман 6", "Кредитование")
        );
        meetingService.create(
                new MeetingCreateDto(getDate(2, 10, 0), "казань бауман 6", "Кредитование")
        );

        assertThrows(WrongDataException.class, () -> meetingService.create(new MeetingCreateDto(getDate(2, 10, 10), "казань бауман 6", "Кредитование")));
        assertThrows(WrongDataException.class, () -> meetingService.create(new MeetingCreateDto(getDate(2, 10, 30), "казань бауман 6", "Кредитование")));

        meetingService.create(new MeetingCreateDto(getDate(3, 10, 10), "казань бауман 6", "Кредитование"));
        meetingService.create(new MeetingCreateDto(getDate(3, 10, 10), "казань бауман 6", "Кредитование"));
        assertThrows(WrongDataException.class, () ->  meetingService.create(new MeetingCreateDto(getDate(3, 10, 10), "казань бауман 6", "Кредитование")));
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
                        .role(Role.ADMIN)
                        .photo("test_photo")
                        .build()
        );
    }

    private Instant getDate(int day, int hour, int minute) {
        return LocalDateTime.of(2024, 4, day, hour, minute).atZone(ZoneId.systemDefault()).toInstant();
    }
}
