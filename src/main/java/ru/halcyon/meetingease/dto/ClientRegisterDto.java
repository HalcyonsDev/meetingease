package ru.halcyon.meetingease.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegisterDto {
    @Size(min = 1, max = 100, message = "Name must be more than 1 character and less than 100 characters.")
    @Pattern(regexp = "[a-zA-Z0-9-]+", message = "Name must contain only letters, digits, and dashes")
    @NotBlank(message = "Name is required")
    private String name;

    @Size(min = 1, max = 100, message = "Surname must be more than 1 character and less than 100 characters.")
    @Pattern(regexp = "[a-zA-Z0-9-]+", message = "Surname must contain only letters, digits, and dashes")
    @NotBlank(message = "Surname is required")
    private String surname;

    @Email(message = "Email is not valid.")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(regexp = "\\+\\d+", message = "Phone number must start with '+' and contain only digits after that")
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Size(min = 1, max = 100, message = "Position must be more than 1 character and less than 100 characters." )
    @NotBlank(message = "Position is required")
    private String position;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String password;
}
