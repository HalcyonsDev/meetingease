package ru.halcyon.meetingease.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.halcyon.meetingease.dto.CompanyCreateDto;
import ru.halcyon.meetingease.dto.MeetingCreateDto;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Meeting;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.repository.CompanyRepository;
import ru.halcyon.meetingease.repository.MeetingRepository;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.company.CompanyService;
import ru.halcyon.meetingease.service.meeting.MeetingService;
import ru.halcyon.meetingease.support.Role;
import ru.halcyon.meetingease.support.Status;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class MeetingControllerTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private CompanyService companyService;

    private static final String CLIENT_EMAIL = "test_email@gmail.com";

    @BeforeEach
    void setUp() {
        meetingRepository.deleteAll();
        clientRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void create() throws Exception {
        createClient();
        setJwtAuth();

        MeetingCreateDto dto = getMeetingDto(true);

        mvc.perform(post("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(dto)))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("date").value(dto.getDate().toString()))
                .andExpect(jsonPath("address").isNotEmpty())
                .andExpect(jsonPath("city").isNotEmpty())
                .andExpect(jsonPath("street").isNotEmpty())
                .andExpect(jsonPath("houseNumber").isNotEmpty())
                .andExpect(jsonPath("status").value(Status.IN_WAITING.toString()))
                .andExpect(jsonPath("agent").isNotEmpty())
                .andExpect(jsonPath("deal").isNotEmpty())
                .andExpect(jsonPath("clients", hasSize(1)));
    }

    @Test
    void createValidatesData() throws Exception {
        createClient();
        setJwtAuth();

        MeetingCreateDto dto = getMeetingDto(false);

        mvc.perform(post("/api/v1/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(dto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("title").value("Bad Request"))
                .andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("detail").value("City is required"));
    }

    @Test
    void cancel() throws Exception {
        createClient();
        setJwtAuth();
        createCompany();

        Meeting meeting = meetingService.create(getMeetingDto(true));

        ResultActions result = mvc.perform(post("/api/v1/meetings/{meetingId}/cancel", meeting.getId()))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        meeting = meetingService.findById(meeting.getId());

        result.andExpect(content().json(getRequestJson(meeting)));
    }

    @Test
    void complete() throws Exception {
        createClient();
        setJwtAuth();
        createCompany();

        Meeting meeting = meetingService.create(getMeetingDto(true));

        ResultActions result = mvc.perform(post("/api/v1/meetings/{meetingId}/complete", meeting.getId()))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        meeting = meetingService.findById(meeting.getId());

        result.andExpect(content().json(getRequestJson(meeting)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"s_Улица Лобачевского", "h_10", "d_Оформление аренды"})
    void changeMeetingData(String input) throws Exception {
        createClient();
        setJwtAuth();
        createCompany();

        Meeting meeting = meetingService.create(getMeetingDto(true));

        String value = input.split("_")[1];
        ResultActions result = mvc.perform(patch("/api/v1/meetings/{meeting}/{request}",
                        meeting.getId(), getRequest(input.split("_")[0]))
                .param("value", value))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        meeting = meetingService.findById(meeting.getId());
        result.andExpect(content().json(getRequestJson(meeting)));
    }

    @Test
    void getAllScheduledMeetings() throws Exception {
        createClient();
        setJwtAuth();
        createCompany();

        meetingService.create(getMeetingDto(true));
        meetingService.create(getMeetingDto(true));

        mvc.perform(get("/api/v1/meetings/in-waiting"))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getFreeDatesForWeek() throws Exception {
        createClient();
        setJwtAuth();
        createCompany();

        meetingService.create(getMeetingDto(true));

        mvc.perform(get("/api/v1/meetings/free-dates")
                 .param("city", "Казань"))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty());
    }

    private String getRequestJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();

        return writer.writeValueAsString(object);
    }

    private void createClient() {
        clientRepository.save(
                Client.builder()
                        .email(CLIENT_EMAIL)
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

    private void setJwtAuth() {
        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, CLIENT_EMAIL, true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
    }

    private MeetingCreateDto getMeetingDto(boolean isValid) {
        return new MeetingCreateDto(
                LocalDateTime.of(2024, LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth() + 1, 12, 30).atZone(ZoneId.systemDefault()).toInstant(),
                isValid ? "Казань" : " ",
                "Бауман",
                "11",
                "Кредитование"
        );
    }

    private void createCompany() {
        companyService.create(new CompanyCreateDto("test-name", "test_description"));
    }

    private String getRequest(String type) {
        switch (type) {
            case "s" -> {
                return "change-street";
            }
            case "h" -> {
                return "change-house";
            }
            default -> {
                return "change-deal";
            }
        }
    }
}
