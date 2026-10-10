package dev.users.dto;

import dev.users.security.UserRole;
import jakarta.validation.constraints.*;

public record CreateUserDto(
        @NotBlank(message = "First name cannot be empty.")
        @Size(min = 1, max = 50, message = "First name shouldn't be longer than fifty symbols and shorter than two symbols.")
        String firstName,

        @NotBlank(message = "Last name cannot be empty.")
        @Size(min = 1, max = 50, message = "First name shouldn't be longer than fifty symbols and shorter than two symbols.")
        String lastName,

        @Email(message = "Email must be in correct format.")
        @Size(max = 75, message = "Email shouldn't be longer than seventy five symbols.")
        String email,

        @NotBlank(message = "Password cannot be empty.")
        @Size(min = 8, message = "Password length min 8 symbols.")
        String rawPassword,

        @NotNull(message = "User role cannot be empty.")
        UserRole userRole

) {
    @Override
    public String toString() {
        return "{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", rawPassword='" + rawPassword + '\'' +
                ", userRole='" + userRole + '\'' +
                '}';
    }
}
