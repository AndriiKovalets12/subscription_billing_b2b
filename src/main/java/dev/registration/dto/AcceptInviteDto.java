package dev.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInviteDto(

        String token,

        @NotBlank(message = "First name cannot be empty.")
        @Size(min = 1, max = 50, message = "First name shouldn't be longer than fifty symbols and shorter than two symbols.")
        String firstName,

        @NotBlank(message = "Last name cannot be empty.")
        @Size(min = 1, max = 50, message = "Last name shouldn't be longer than fifty symbols and shorter than two symbols.")
        String lastName,

        @NotBlank(message = "Password cannot be empty.")
        @Size(min = 8, message = "Password length min 8 symbols.")
        String rawPassword
) {
}
