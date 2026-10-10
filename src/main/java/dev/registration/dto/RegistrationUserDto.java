package dev.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationUserDto(
        @NotBlank(message = "First name cannot be empty.")
        @Size(min = 1, max = 50, message = "First name shouldn't be longer than fifty symbols and shorter than two symbols.")
        String firstName,

        @NotBlank(message = "Last name cannot be empty.")
        @Size(min = 1, max = 50, message = "Last name shouldn't be longer than fifty symbols and shorter than two symbols.")
        String lastName,

        @Email(message = "Email must be in correct format.")
        @Size(max = 75, message = "Email shouldn't be longer than seventy five symbols.")
        String email,

        @NotBlank(message = "Password cannot be empty.")
        @Size(min = 8, message = "Password length min 8 symbols.")
        String rawPassword,

        @NotBlank(message = "Tenant name cannot be empty.")
        String tenantName
) {
    @Override
    public String toString() {
        return "{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", rawPassword='" + rawPassword + '\'' +
                ", tenantName=" + tenantName +
                '}';
    }
}
