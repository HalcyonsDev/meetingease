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

    @Size(min = 1, max = 100, message = "City must be more than 1 character and less than 100 characters.")
    @NotBlank(message = "City is required")
    private String city;

    @Size(min = 1, max = 100, message = "Street must be more than 1 character and less than 100 characters.")
    @NotBlank(message = "Street is required")
    private String street;

    @Size(min = 1, max = 20, message = "House number must be more than 1 character and less than 20 characters.")
    @NotBlank(message = "HouseNumber is required")
    private String houseNumber;

    @Size(min = 1, max = 100, message = "Deal type must be more than 1 character and less than 100 characters.")
    @NotBlank(message = "Deal type is required")
    private String dealType;
}
