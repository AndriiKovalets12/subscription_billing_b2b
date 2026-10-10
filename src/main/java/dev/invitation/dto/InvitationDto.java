package dev.invitation.dto;

import dev.users.security.UserRole;

import java.time.OffsetDateTime;

public record InvitationDto(
        String token,
        String email,
        UserRole userRole,
        Long tenantId,
        OffsetDateTime expiresAt
) {
    @Override
    public String toString() {
        return "{" +
                "token='" + token + '\'' +
                ", email='" + email + '\'' +
                ", userRole=" + userRole +
                ", tenantId=" + tenantId +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
