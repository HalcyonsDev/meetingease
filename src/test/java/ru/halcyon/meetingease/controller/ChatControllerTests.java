package ru.halcyon.meetingease.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.halcyon.meetingease.dto.ChatMessageCreateDto;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.ChatMessage;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Meeting;
import ru.halcyon.meetingease.repository.*;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.agent.AgentService;
import ru.halcyon.meetingease.service.chat.ChatMessageService;
import ru.halcyon.meetingease.support.Role;
import ru.halcyon.meetingease.support.Status;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class ChatControllerTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private AgentService agentService;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    private static final String AGENT_EMAIL = "ivan.ivanov@example.com";
    private static final String OWNER_EMAIL = "owner_email@gmail.com";

    @BeforeEach
    void setUp() {
        meetingRepository.deleteAll();
        clientRepository.deleteAll();
        chatMessageRepository.deleteAll();
    }

    @Test
    void processMessageByClientSender() throws Exception {
        Client sender = createClient();
        setJwtAuth(true);

        Agent recipient = agentService.findByEmail(AGENT_EMAIL);
        createMeeting(sender, recipient);

        ChatMessageCreateDto dto = new ChatMessageCreateDto(recipient.getId(), "test_content");

        sendProcessMessageRequest(dto, sender.getId(), recipient.getId(), true);
    }

    @Test
    void processMessageByAgentSender() throws Exception {
        Agent sender = agentService.findByEmail(AGENT_EMAIL);
        setJwtAuth(false);

        Client recipient = createClient();
        createMeeting(recipient, sender);

        ChatMessageCreateDto dto = new ChatMessageCreateDto(recipient.getId(), "test_content");

        sendProcessMessageRequest(dto, sender.getId(), recipient.getId(), false);
    }

    @Test
    void processMessageValidatesData() throws Exception {
        Client sender = createClient();
        setJwtAuth(true);

        Agent recipient = agentService.findByEmail(AGENT_EMAIL);
        createMeeting(sender, recipient);

        ChatMessageCreateDto dto = new ChatMessageCreateDto(recipient.getId(), " ");

        mvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(dto)))
                .andExpect(status().is4xxClientError())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("title").value("Bad Request"))
                .andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("detail").value("Content is required"));
    }

    @Test
    void findByChatRoom() throws Exception {
        Client sender = createClient();
        setJwtAuth(true);

        Agent recipient = agentService.findByEmail(AGENT_EMAIL);
        createMeeting(sender, recipient);

        ChatMessageCreateDto dto = new ChatMessageCreateDto(recipient.getId(), "test_content");
        ChatMessage message1 = chatMessageService.processMessage(dto);
        ChatMessage message2 = chatMessageService.processMessage(dto);

        mvc.perform(get("/api/v1/messages/recipient/{recipientId}", recipient.getId()))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("content", hasSize(2)))
                .andExpect(jsonPath("content[0].id").value(message1.getId()))
                .andExpect(jsonPath("content[0].createdAt").value(message1.getCreatedAt().toString()))
                .andExpect(jsonPath("content[0].content").value(message1.getContent()))
                .andExpect(jsonPath("content[0].senderId").value(message1.getSenderId()))
                .andExpect(jsonPath("content[0].recipientId").value(message1.getRecipientId()))
                .andExpect(jsonPath("content[0].isSenderClient").value(message1.getIsSenderClient()))
                .andExpect(jsonPath("content[1].id").value(message2.getId()))
                .andExpect(jsonPath("content[1].createdAt").value(message2.getCreatedAt().toString()))
                .andExpect(jsonPath("content[1].content").value(message2.getContent()))
                .andExpect(jsonPath("content[1].senderId").value(message2.getSenderId()))
                .andExpect(jsonPath("content[1].recipientId").value(message2.getRecipientId()))
                .andExpect(jsonPath("content[1].isSenderClient").value(message2.getIsSenderClient()));
    }

    private Client createClient() {
        return clientRepository.save(
                Client.builder()
                        .email(OWNER_EMAIL)
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

    private void setJwtAuth(boolean isClient) {
        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, isClient ? OWNER_EMAIL : AGENT_EMAIL, isClient);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
    }

    private void createMeeting(Client client, Agent agent) {
        meetingRepository.save(
                Meeting.builder()
                        .date(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
                        .status(Status.IN_WAITING)
                        .address("test_address")
                        .city("test_city")
                        .street("test_street")
                        .houseNumber("test_house")
                        .deal(dealRepository.findById(1L).get())
                        .clients(List.of(client))
                        .agent(agent)
                        .build()
        );
    }

    private String getRequestJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();

        return writer.writeValueAsString(object);
    }

    private void sendProcessMessageRequest(ChatMessageCreateDto dto, long senderId, long recipientId, boolean isSenderClient) throws Exception {
        mvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(dto)))
                .andExpect(status().isOk())
                .andExpect(content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("createdAt").isNotEmpty())
                .andExpect(jsonPath("content").value(dto.getContent()))
                .andExpect(jsonPath("senderId").value(senderId))
                .andExpect(jsonPath("recipientId").value(recipientId))
                .andExpect(jsonPath("isSenderClient").value(isSenderClient));
    }
}
