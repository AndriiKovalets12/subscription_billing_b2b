package dev.invitation.dto;

import dev.users.security.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInvitationDto(

        @NotBlank(message = "Email shouldn't be null or empty.")
        @Email
        String email,

        @NotNull(message = "User role shouldn't be null.")
        UserRole userRole,

        @NotNull(message = "Tenant id shouldn't be null")
        Long tenantId
) {

    @Override
    public String toString() {
        return "{" +
                "email='" + email + '\'' +
                ", userRole=" + userRole +
                ", tenantId=" + tenantId +
                '}';
    }
}
