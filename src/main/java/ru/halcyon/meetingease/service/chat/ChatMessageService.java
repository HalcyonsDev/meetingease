package ru.halcyon.meetingease.service.chat;

import org.springframework.data.domain.Page;
import ru.halcyon.meetingease.dto.ChatMessageCreateDto;
import ru.halcyon.meetingease.model.ChatMessage;

public interface ChatMessageService {
    ChatMessage save(ChatMessage chatMessage);
    ChatMessage processMessage(ChatMessageCreateDto dto);
    Page<ChatMessage> findChatMessages(Long recipientId, Integer offset, Integer limit);
    ChatMessage findById(Long chatMessageId);
}
