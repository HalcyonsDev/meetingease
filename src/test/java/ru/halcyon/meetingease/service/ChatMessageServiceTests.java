package ru.halcyon.meetingease.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.halcyon.meetingease.TestPostgresContainer;
import ru.halcyon.meetingease.dto.ChatMessageCreateDto;
import ru.halcyon.meetingease.exception.ResourceForbiddenException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.ChatMessage;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Meeting;
import ru.halcyon.meetingease.repository.*;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.chat.ChatMessageService;
import ru.halcyon.meetingease.support.Role;
import ru.halcyon.meetingease.support.Status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChatMessageServiceTests {
    @Container
    public static PostgreSQLContainer<?> postgres = TestPostgresContainer.getInstance();

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private DealRepository dealRepository;

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
        chatMessageRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void findById() {
        ChatMessage createdChatMessage = chatMessageRepository.save(ChatMessage.builder()
                .content("test_content")
                .senderId(createClient().getId())
                .recipientId(1L)
                .isSenderClient(true)
                .build());

        ChatMessage receivedChatMessage = chatMessageService.findById(createdChatMessage.getId());

        assertThat(receivedChatMessage).isNotNull();
        assertThat(receivedChatMessage).isEqualTo(createdChatMessage);
    }

    @Test
    void processClientMessage() {
        Client client = createClient();
        createMeeting(client);

        setJwtAuth(client.getEmail(), true);

        ChatMessage chatMessage = chatMessageService.processMessage(new ChatMessageCreateDto(1L, "test_content"));

        assertThat(chatMessage.getContent()).isEqualTo("test_content");
        assertThat(chatMessage.getSenderId()).isEqualTo(client.getId());
        assertThat(chatMessage.getRecipientId()).isEqualTo(1L);
        assertThat(chatMessage.getIsSenderClient()).isTrue();
    }

    @Test
    void processClientMessage_ChecksMeetingExisting() {
        Client client = createClient();

        setJwtAuth(client.getEmail(), true);

        ResourceForbiddenException forbiddenException = assertThrows(ResourceForbiddenException.class, () ->
                chatMessageService.processMessage(new ChatMessageCreateDto(1L, "test_content")));

        assertThat(forbiddenException.getMessage()).isEqualTo("No access for chat.");
    }

    @Test
    void processAgentMessage() {
        Client client = createClient();
        createMeeting(client);
        Agent agent = agentRepository.findById(1L).get();

        setJwtAuth(agent.getEmail(), false);

        ChatMessage chatMessage = chatMessageService.processMessage(new ChatMessageCreateDto(client.getId(), "test_content"));

        assertThat(chatMessage.getContent()).isEqualTo("test_content");
        assertThat(chatMessage.getSenderId()).isEqualTo(agent.getId());
        assertThat(chatMessage.getRecipientId()).isEqualTo(client.getId());
        assertThat(chatMessage.getIsSenderClient()).isFalse();
    }

    @Test
    void processAgentMessage_ChecksMeetingExisting() {
        Client client = createClient();
        Agent agent = agentRepository.findById(1L).get();

        setJwtAuth(agent.getEmail(), false);

        ResourceForbiddenException forbiddenException = assertThrows(ResourceForbiddenException.class, () ->
                chatMessageService.processMessage(new ChatMessageCreateDto(client.getId(), "test_content")));

        assertThat(forbiddenException.getMessage()).isEqualTo("No access for chat.");
    }

    @Test
    void processClientMessages() {
        Client client = createClient();
        createMeeting(client);

        setJwtAuth(client.getEmail(), true);

        ChatMessage chatMessage1 = chatMessageService.processMessage(new ChatMessageCreateDto(1L, "test_content"));
        ChatMessage chatMessage2 = chatMessageService.processMessage(new ChatMessageCreateDto(1L, "test_content"));
        ChatMessage chatMessage3 = chatMessageService.processMessage(new ChatMessageCreateDto(1L, "test_content"));

        List<ChatMessage> messages = chatMessageService.findChatMessages(1L, 0, 20).getContent();

        assertThat(messages).isEqualTo(List.of(chatMessage1, chatMessage2, chatMessage3));
    }

    @Test
    void processClientMessages_ChecksMeetingExisting() {
        Client client = createClient();
        setJwtAuth(client.getEmail(), true);

        ResourceForbiddenException forbiddenException = assertThrows(ResourceForbiddenException.class, () ->
                chatMessageService.findChatMessages(1L, 0, 20));

        assertThat(forbiddenException.getMessage()).isEqualTo("No access for chat.");
    }

    @Test
    void processAgentMessages() {
        Client client = createClient();
        createMeeting(client);
        Agent agent = agentRepository.findById(1L).get();

        setJwtAuth(agent.getEmail(), false);

        ChatMessage chatMessage1 = chatMessageService.processMessage(new ChatMessageCreateDto(client.getId(), "test_content"));
        ChatMessage chatMessage2 = chatMessageService.processMessage(new ChatMessageCreateDto(client.getId(), "test_content"));
        ChatMessage chatMessage3 = chatMessageService.processMessage(new ChatMessageCreateDto(client.getId(), "test_content"));

        List<ChatMessage> messages = chatMessageService.findChatMessages(client.getId(), 0, 20).getContent();

        assertThat(messages).isEqualTo(List.of(chatMessage1, chatMessage2, chatMessage3));
    }

    @Test
    void processAgentMessages_ChecksMeetingExisting() {
        Client client = createClient();
        Agent agent = agentRepository.findById(1L).get();

        setJwtAuth(agent.getEmail(), false);

        ResourceForbiddenException forbiddenException = assertThrows(ResourceForbiddenException.class, () ->
                chatMessageService.findChatMessages(client.getId(), 0, 20));

        assertThat(forbiddenException.getMessage()).isEqualTo("No access for chat.");
    }

    private Client createClient() {
        return clientRepository.save(
                Client.builder()
                        .email("test_email@gmail.com")
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

    private Meeting createMeeting(Client client) {
        Agent agent = agentRepository.findById(1L).get();

        return meetingRepository.save(
                Meeting.builder()
                        .date(getDate(2, 14, 30))
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

    private void setJwtAuth(String email, boolean isClient) {
        JwtAuthentication jwtAuthentication = new JwtAuthentication(true, email, isClient);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
    }

    private Instant getDate(int day, int hour, int minute) {
        return LocalDateTime.of(2024, 4, day, hour, minute).atZone(ZoneId.systemDefault()).toInstant();
    }
}
