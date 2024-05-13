package ru.halcyon.meetingease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCreateDto {
    @Size(min = 1, max = 100, message = "Name must be more than 1 character and less than 100 characters.")
    @Pattern(regexp = "[a-zA-Z0-9-]+", message = "Name must contain only letters, digits, and dashes")
    @NotBlank(message = "Name is required")
    private String name;

    @Size(min = 1, max = 500, message = "Description must be more than 1 character and less than 500 characters.")
    @NotBlank(message = "Description is required")
    private String description;
}
