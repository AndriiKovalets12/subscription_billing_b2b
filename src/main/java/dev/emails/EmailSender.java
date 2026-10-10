package dev.emails;

import dev.invitation.dto.InvitationDto;
import dev.tenants.dto.TenantDto;

public interface EmailSender {
    void sendInvitationEmail(InvitationDto invite, TenantDto tenantDto);
}
