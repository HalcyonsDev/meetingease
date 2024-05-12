package ru.halcyon.meetingease.controller;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.SerializationFeature;
import ru.halcyon.meetingease.security.AuthRequest;
import ru.halcyon.meetingease.security.RefreshRequest;
import ru.halcyon.meetingease.service.auth.agent.AgentAuthService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
public class AgentAuthControllerTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private AgentAuthService agentAuthService;

    @Test
    void login() throws Exception {
        mvc.perform(post("/api/v1/agents/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(getAuthRequest())))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken").isNotEmpty())
                .andExpect(jsonPath("refreshToken").isNotEmpty())
                .andExpect(jsonPath("type").value("Bearer"));
    }

    @Test
    void getAccessToken() throws Exception {
        String refreshToken = agentAuthService.login(getAuthRequest()).getRefreshToken();

        mvc.perform(post("/api/v1/agents/auth/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(new RefreshRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken").isNotEmpty())
                .andExpect(jsonPath("refreshToken").isEmpty())
                .andExpect(jsonPath("type").value("Bearer"));
    }

    @Test
    void getRefreshToken() throws Exception {
        String refreshToken = agentAuthService.login(getAuthRequest()).getRefreshToken();

        mvc.perform(post("/api/v1/agents/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(new RefreshRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accessToken").isNotEmpty())
                .andExpect(jsonPath("refreshToken").isNotEmpty())
                .andExpect(jsonPath("type").value("Bearer"));
    }

    private AuthRequest getAuthRequest() {
        return new AuthRequest("ivan.ivanov@example.com", "TestPassword123");
    }

    private String getRequestJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();

        return writer.writeValueAsString(object);
    }
}
