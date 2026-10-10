package dev.users.dto;

import dev.users.security.UserRole;
import jakarta.validation.constraints.Email;

public record FiltersUserDto(
        String firstName,
        String lastName,

        @Email
        String email,

        UserRole userRole
) {
    @Override
    public String toString() {
        return "{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", userRole=" + userRole +
                '}';
    }
}
