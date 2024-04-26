package ru.halcyon.meetingease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.halcyon.meetingease.dto.ChatMessageCreateDto;
import ru.halcyon.meetingease.model.ChatMessage;
import ru.halcyon.meetingease.service.chat.ChatMessageService;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;

    @PostMapping
    public ResponseEntity<ChatMessage> processMessage(@RequestBody @Valid ChatMessageCreateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ChatMessage message = chatMessageService.processMessage(dto);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<Page<ChatMessage>> findByChatRoom(
            @PathVariable Long recipientId,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit
    ) {
        Page<ChatMessage> messages = chatMessageService.findChatMessages(recipientId, offset, limit);
        return ResponseEntity.ok(messages);
    }
}
