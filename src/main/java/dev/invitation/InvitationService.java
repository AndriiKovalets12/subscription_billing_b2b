package dev.invitation;

import dev.emails.EmailService;
import dev.invitation.data.InvitationEntity;
import dev.invitation.data.InvitationRepository;
import dev.invitation.dto.CreateInvitationDto;
import dev.invitation.dto.InvitationDto;
import dev.security.SecurityUtils;
import dev.tenants.TenantService;
import dev.tenants.dto.TenantDto;
import dev.users.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class InvitationService {
    private final InvitationRepository invitationRepository;
    private final UserService userService;
    private final TenantService tenantService;
    private final EmailService emailService;

    public InvitationService(InvitationRepository invitationRepository,
                             UserService userService,
                             TenantService tenantService, EmailService emailService) {
        this.invitationRepository = invitationRepository;
        this.userService = userService;
        this.tenantService = tenantService;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public InvitationDto findByToken(String token){
        InvitationEntity invitation = invitationRepository
                .findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invitation with token:" + token + " not found."));

        return mapperToDto(invitation);
    }

    @Transactional
    public InvitationDto create(@Valid CreateInvitationDto invitation) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        if (!Objects.equals(currentTenantId, invitation.tenantId())){
            throw new IllegalStateException();
        }

        if (userService.exists(invitation.email())) {
            throw new EntityExistsException("User with email:" + invitation.email() + " already exists.");
        }

        String token = UUID.randomUUID().toString();

        InvitationEntity createdInvitation = new InvitationEntity(
                token,
                invitation.email(),
                invitation.userRole(),
                tenantService.getTenantBy(currentTenantId),
                OffsetDateTime.now().plusDays(3)
        );

        invitationRepository.save(createdInvitation);

        InvitationDto invitationDto = mapperToDto(createdInvitation);
        TenantDto tenantDto = tenantService.getById(currentTenantId);

        emailService.sendInvitationEmail(invitationDto, tenantDto);

        return invitationDto;
    }

    @Transactional
    public void delete(InvitationDto invite) {
        InvitationEntity invitationToDelete = invitationRepository.
                findByToken(invite.token())
                .orElseThrow(() -> new EntityNotFoundException("Invitation with token:" + invite.token() + " not found."));

        invitationRepository.delete(invitationToDelete);
    }
    private InvitationDto mapperToDto(InvitationEntity entity){
        return new InvitationDto(
                entity.getToken(),
                entity.getEmail(),
                entity.getUserRole(),
                entity.getTenant().getId(),
                entity.getExpiresAt());
    }

   
}
