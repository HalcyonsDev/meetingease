package ru.halcyon.meetingease.service.chat;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.dto.ChatMessageCreateDto;
import ru.halcyon.meetingease.event.message.MessageCreatedEvent;
import ru.halcyon.meetingease.exception.ResourceForbiddenException;
import ru.halcyon.meetingease.exception.ResourceNotFoundException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.ChatMessage;
import ru.halcyon.meetingease.repository.ChatMessageRepository;
import ru.halcyon.meetingease.security.AuthenticatedDataProvider;
import ru.halcyon.meetingease.service.agent.AgentService;
import ru.halcyon.meetingease.service.auth.AgentAuthService;
import ru.halcyon.meetingease.service.auth.ClientAuthService;
import ru.halcyon.meetingease.service.client.ClientService;
import ru.halcyon.meetingease.service.meeting.MeetingService;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    private final ClientAuthService clientAuthService;
    private final AgentAuthService agentAuthService;
    private final ClientService clientService;
    private final AgentService agentService;
    private final MeetingService meetingService;
    private final AuthenticatedDataProvider authenticatedDataProvider;

    private final ApplicationEventPublisher eventPublisher;

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public ChatMessage processMessage(ChatMessageCreateDto dto) {
        ChatMessage message = authenticatedDataProvider.getIsClient() ? processClientMessage(dto) : processAgentMessage(dto);
        sendMessage(message);

        return message;
    }

    public Page<ChatMessage> findChatMessages(Long recipientId, Integer offset, Integer limit) {
        return authenticatedDataProvider.getIsClient() ? processClientMessages(recipientId, offset, limit) : processAgentMessages(recipientId, offset, limit);
    }

    private ChatMessage processClientMessage(ChatMessageCreateDto dto) {
        clientService.isVerifiedClient();

        Client client = clientService.findByEmail(authenticatedDataProvider.getEmail());
        Agent agent = agentService.findById(dto.getRecipientId());

        isThereMeeting(client, agent);

        return save(getChatMessage(dto.getContent(), client.getId(), agent.getId(), true));
    }

    private ChatMessage processAgentMessage(ChatMessageCreateDto dto) {
        Agent agent = agentService.findByEmail(agentAuthService.getAuthInfo().getEmail());
        Client client = clientService.findById(dto.getRecipientId());

        isThereMeeting(client, agent);

        return save(getChatMessage(dto.getContent(), agent.getId(), client.getId(), false));
    }

    private Page<ChatMessage> processClientMessages(long recipientId, Integer offset, Integer limit) {
        clientService.isVerifiedClient();

        Client client = clientService.findByEmail(authenticatedDataProvider.getEmail());
        Agent agent = agentService.findById(recipientId);

        isThereMeeting(client, agent);

        return chatMessageRepository.findAllBySenderIdAndRecipientIdAndIsSenderClient(client.getId(), agent.getId(), true,
                PageRequest.of(offset, limit, Sort.by(Sort.Direction.ASC, "id")));
    }

    private Page<ChatMessage> processAgentMessages(long recipientId, Integer offset, Integer limit) {
        Agent agent = agentService.findByEmail(agentAuthService.getAuthInfo().getEmail());
        Client client = clientService.findById(recipientId);

        isThereMeeting(client, agent);

        return chatMessageRepository.findAllBySenderIdAndRecipientIdAndIsSenderClient(agent.getId(), client.getId(), false,
                PageRequest.of(offset, limit, Sort.by(Sort.Direction.ASC, "id")));
    }

    
    public ChatMessage findById(Long chatMessageId) {
        return chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat message with this id not found."));
    }

    private void isThereMeeting(Client client, Agent agent) {
        if (Boolean.FALSE.equals(meetingService.existsByAgentAndClient(agent, client))) {
            throw new ResourceForbiddenException("No access for chat.");
        }
    }

    private ChatMessage getChatMessage(String content, long senderId, long recipientId, boolean isSenderClient) {
        return ChatMessage.builder()
                .content(content)
                .senderId(senderId)
                .recipientId(recipientId)
                .isSenderClient(isSenderClient)
                .build();
    }

    private void sendMessage(ChatMessage message) {
        eventPublisher.publishEvent(new MessageCreatedEvent(message));
    }
}
