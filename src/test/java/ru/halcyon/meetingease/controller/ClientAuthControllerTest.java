package ru.halcyon.meetingease.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.SerializationFeature;
import ru.halcyon.meetingease.dto.ClientRegisterDto;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.RefreshRequest;
import ru.halcyon.meetingease.service.auth.client.ClientAuthService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
public class ClientAuthControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientAuthService clientAuthService;

    @BeforeEach
    public void setUp() {
        clientRepository.deleteAll();
    }

    @Test
    void register() throws Exception {
        mvc.perform(post("/api/v1/clients/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(getClientDto())))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken").isNotEmpty())
                .andExpect(jsonPath("refreshToken").isNotEmpty())
                .andExpect(jsonPath("type").value("Bearer"));
    }

    @Test
    void registerValidatesData() throws Exception {
        ClientRegisterDto dto = new ClientRegisterDto(
                "test_name",
                "test-surname",
                "testemail@gmail.com",
                "+77777777",
                "test-position",
                "TestPassword123"
        );

        mvc.perform(post("/api/v1/clients/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(dto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("title").value("Bad Request"))
                .andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("detail").value("Name must contain only letters, digits, and dashes"));
    }

    @Test
    void confirmEmail() throws Exception {
        String accessToken = clientAuthService.register(getClientDto()).getAccessToken();

        mvc.perform(post("/api/v1/clients/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", accessToken))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string("Account is verified"));
    }

    @Test
    void login() throws Exception {
        ClientRegisterDto dto = getClientDto();
        clientAuthService.register(dto);

        AuthRequest request = new AuthRequest(dto.getEmail(), dto.getPassword());

        mvc.perform(post("/api/v1/clients/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(request)))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken").isNotEmpty())
                .andExpect(jsonPath("refreshToken").isNotEmpty())
                .andExpect(jsonPath("type").value("Bearer"));
    }

    @Test
    void getAccessToken() throws Exception {
        String refreshToken = clientAuthService.register(getClientDto()).getRefreshToken();
        RefreshRequest request = new RefreshRequest(refreshToken);

        mvc.perform(post("/api/v1/clients/auth/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(request)))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken").isNotEmpty())
                .andExpect(jsonPath("refreshToken").isEmpty())
                .andExpect(jsonPath("type").value("Bearer"));
    }

    @Test
    void getRefreshToken() throws Exception {
        String refreshToken = clientAuthService.register(getClientDto()).getRefreshToken();
        RefreshRequest request = new RefreshRequest(refreshToken);

        mvc.perform(post("/api/v1/clients/auth/refresh")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(getRequestJson(request)))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken").isNotEmpty())
                .andExpect(jsonPath("refreshToken").isNotEmpty())
                .andExpect(jsonPath("type").value("Bearer"));
    }

    private ClientRegisterDto getClientDto() {
        return new ClientRegisterDto(
                "test-name",
                "test-surname",
                "testemail@gmail.com",
                "+77777777",
                "test-position",
                "TestPassword123"
        );
    }

    private String getRequestJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();

        return writer.writeValueAsString(object);
    }
}
