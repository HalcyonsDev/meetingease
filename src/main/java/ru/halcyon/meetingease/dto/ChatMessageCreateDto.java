package ru.halcyon.meetingease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageCreateDto {
    private Long recipientId;

    @Size(min = 1, max = 500, message = "Content must be more than 1 character and less than 500 characters.")
    @NotBlank(message = "Content is required")
    private String content;
}
