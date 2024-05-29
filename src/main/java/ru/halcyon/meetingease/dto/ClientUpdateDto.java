package ru.halcyon.meetingease.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClientUpdateDto {
    @Size(min = 1, max = 100, message = "Name must be more than 1 character and less than 100 characters.")
    @Pattern(regexp = "[a-zA-Z0-9-]+", message = "Name must contain only letters, digits, and dashes")
    private String name;

    @Size(min = 1, max = 100, message = "Surname must be more than 1 character and less than 100 characters.")
    @Pattern(regexp = "[a-zA-Z0-9-]+", message = "Surname must contain only letters, digits, and dashes")
    private String surname;

    @Email(message = "Email is not valid.")
    private String email;

    @Pattern(regexp = "\\+\\d+", message = "Phone number must start with '+' and contain only digits after that")
    private String phoneNumber;

    @Size(min = 1, max = 100, message = "Position must be more than 1 character and less than 100 characters." )
    private String position;
}
