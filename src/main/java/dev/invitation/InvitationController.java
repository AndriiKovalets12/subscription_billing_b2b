package dev.invitation;

import dev.invitation.dto.CreateInvitationDto;
import dev.invitation.dto.InvitationDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<InvitationDto> inviteUser(@Valid @RequestBody CreateInvitationDto invitation){

        log.info("Called inviteUser(CreateInvitationDto invitation) with invitation:{}.", invitation.toString());

        InvitationDto invitationDto = invitationService.create(invitation);

        return ResponseEntity.status(HttpStatus.CREATED).body(invitationDto);
    }




}
