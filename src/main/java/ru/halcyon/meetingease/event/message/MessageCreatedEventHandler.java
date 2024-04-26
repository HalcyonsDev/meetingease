package ru.halcyon.meetingease.event.message;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import ru.halcyon.meetingease.model.ChatMessage;

@Component
@RequiredArgsConstructor
public class MessageCreatedEventHandler {
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleMessageCreatedEvent(MessageCreatedEvent event) {
        ChatMessage message = event.getMessage();

        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getRecipientId()),
                "/queue/messages",
                message
        );
    }
}
