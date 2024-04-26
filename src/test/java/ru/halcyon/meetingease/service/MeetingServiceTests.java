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
import ru.halcyon.meetingease.dto.CompanyCreateDto;
import ru.halcyon.meetingease.dto.MeetingCreateDto;
import ru.halcyon.meetingease.exception.WrongDataException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Deal;
import ru.halcyon.meetingease.model.Meeting;
import ru.halcyon.meetingease.support.Role;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.repository.CompanyRepository;
import ru.halcyon.meetingease.repository.DealRepository;
import ru.halcyon.meetingease.repository.MeetingRepository;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.company.CompanyService;
import ru.halcyon.meetingease.service.meeting.MeetingService;

import java.time.Instant;
import java.time.LocalDateTime;
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
    private DealRepository dealRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private CompanyService companyService;

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
    void createMeeting_ChooseFreeAgent() {
        Client client = createClient("test_email@gmail.com");

        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, client.getEmail(), true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        meetingService.create(
                new MeetingCreateDto(getDate(2, 9, 30), "казань", "бауман", "31/12", "Кредитование")
        );
        meetingService.create(
                new MeetingCreateDto(getDate(2, 10, 0), "казань", "бауман", "31/12", "Кредитование")
        );

        assertThrows(WrongDataException.class, () -> meetingService.create(new MeetingCreateDto(getDate(2, 10, 10), "казань", "бауман", "31/12", "Кредитование")));
        assertThrows(WrongDataException.class, () -> meetingService.create(new MeetingCreateDto(getDate(2, 10, 30), "казань", "бауман", "31/12", "Кредитование")));

        meetingService.create(new MeetingCreateDto(getDate(3, 10, 10), "казань", "бауман", "31/12", "Кредитование"));
        meetingService.create(new MeetingCreateDto(getDate(3, 10, 10), "казань", "бауман", "31/12", "Кредитование"));
        assertThrows(WrongDataException.class, () ->  meetingService.create(new MeetingCreateDto(getDate(3, 10, 10), "казань", "бауман", "31/12", "Кредитование")));
    }

    @Test
    void getFreeDatesForWeek() {
        Client client = createClient("test_email@gmail.com");

        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, client.getEmail(), true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        meetingService.create(new MeetingCreateDto(getDate(23, 10, 0), "казань", "бауман", "31/12", "Кредитование"));
        meetingService.create(new MeetingCreateDto(getDate(23, 17, 30), "казань", "бауман", "31/12", "Кредитование"));
        meetingService.create(new MeetingCreateDto(getDate(24, 10, 30), "казань", "бауман", "31/12", "Кредитование"));

        meetingService.getFreeDatesForWeek("Казань");
    }

    @Test
    void changeStreet() {
        Client client = createClient("test_email@gmail.com");
        String newDisplayName = "12, улица Лобачевского, Вахитовский район, Казань, городской округ Казань, Татарстан, Приволжский федеральный округ, 420111, Россия";
        String newStreet = "улица Лобачевского";

        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, client.getEmail(), true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        companyService.create(new CompanyCreateDto("test_name", "test_description"));

        Meeting meeting = meetingService.create(new MeetingCreateDto(getDate(19, 10, 0), "казань", "кремлевская", "12", "Кредитование"));
        meeting = meetingService.changeStreet(meeting.getId(), "лобачевского");

        assertThat(meeting.getStreet()).isEqualTo(newStreet);
        assertThat(meeting.getAddress()).isEqualTo(newDisplayName);
    }

    @Test
    void changeHouseNumber() {
        Client client = createClient("test_email@gmail.com");
        String newDisplayName = "Министерство строительства, архитектуры и ЖКХ Республики Татарстан, 13, Кремлёвская улица, Вахитовский район, Казань, городской округ Казань, Татарстан, Приволжский федеральный округ, 420111, Россия";
        String newHouseNumber = "13";

        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, client.getEmail(), true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        companyService.create(new CompanyCreateDto("test_name", "test_description"));

        Meeting meeting = meetingService.create(new MeetingCreateDto(getDate(19, 10, 0), "казань", "кремлевская", "12", "Кредитование"));
        meeting = meetingService.changeHouseNumber(meeting.getId(), newHouseNumber);

        assertThat(meeting.getHouseNumber()).isEqualTo(newHouseNumber);
        assertThat(meeting.getAddress()).isEqualTo(newDisplayName);
    }

    @Test
    void changeDeal() {
        Client client = createClient("test_email@gmail.com");
        Deal deal = dealRepository.findByType("Открытие банковского счёта").get();

        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, client.getEmail(), true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);

        companyService.create(new CompanyCreateDto("test_name", "test_description"));

        Meeting meeting = meetingService.create(new MeetingCreateDto(getDate(19, 10, 0), "казань", "кремлевская", "12", "Кредитование"));
        meeting = meetingService.changeDeal(meeting.getId(), "Открытие банковского счёта");

        assertThat(meeting.getDeal()).isEqualTo(deal);
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

    private Instant getDate(int day, int hour, int minute) {
        return LocalDateTime.of(2024, 4, day, hour, minute).atZone(ZoneId.systemDefault()).toInstant();
    }
}
