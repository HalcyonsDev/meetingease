package ru.halcyon.meetingease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCreateDto {
    private Instant date;

    @Size(min = 1, max = 100, message = "Address must be more than 1 character and less than 100 characters.")
    @NotBlank(message = "Address is required")
    private String address;

    @Size(min = 1, max = 100, message = "Deal type must be more than 1 character and less than 100 characters.")
    @NotBlank(message = "Deal type is required")
    private String dealType;
}
