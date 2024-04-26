package ru.halcyon.meetingease.event.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.halcyon.meetingease.model.ChatMessage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreatedEvent {
    private ChatMessage message;
}
