package dev.registration;

import dev.invitation.InvitationService;
import dev.invitation.dto.InvitationDto;
import dev.registration.dto.AcceptInviteDto;
import dev.registration.dto.RegistrationUserDto;
import dev.tenants.TenantService;
import dev.tenants.dto.CreateTenantDto;
import dev.tenants.dto.TenantDto;
import dev.users.UserService;
import dev.users.dto.CreateUserDto;
import dev.users.security.UserRole;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class RegistrationService {
    private final TenantService tenantService;
    private final UserService userService;
    private final InvitationService invitationService;

    public RegistrationService(TenantService tenantService, UserService userService, InvitationService invitationService) {
        this.tenantService = tenantService;
        this.userService = userService;
        this.invitationService = invitationService;
    }

    @Transactional
    public void registerWorkspace(RegistrationUserDto request){

        if (tenantService.existsByName(request.tenantName())){
            throw new EntityExistsException("Tenant with name:" + request.tenantName() + " already exists.");
        }

        TenantDto tenant = tenantService.create(new CreateTenantDto(request.tenantName()));

        CreateUserDto ownerDto = new CreateUserDto(request.firstName(),
                request.lastName(),
                request.email(),
                request.rawPassword(),
                UserRole.TENANT_OWNER);

        userService.registerNewUser(ownerDto, tenant.id());
    }

    @Transactional
    public void acceptInvitation(AcceptInviteDto request) {
        InvitationDto invite = invitationService.findByToken(request.token());

        if (invite.expiresAt().isBefore(OffsetDateTime.now())) {
            invitationService.delete(invite);
            throw new IllegalStateException("Invitation expired.");
        }

        CreateUserDto userDto = new CreateUserDto(
                request.firstName(),
                request.lastName(),
                invite.email(),
                request.rawPassword(),
                invite.userRole()
        );

        userService.registerNewUser(userDto, invite.tenantId());

        invitationService.markAsAccepted(request.token());
    }
}
