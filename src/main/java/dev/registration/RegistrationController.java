package dev.registration;


import dev.registration.dto.AcceptInviteDto;
import dev.registration.dto.RegistrationUserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/v1/auth")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegistrationUserDto request) {
        registrationService.registerWorkspace(request);
        return
                ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PostMapping("/accept-invite")
    public ResponseEntity<Void> acceptInvite(@Valid @RequestBody AcceptInviteDto request) {

        registrationService.acceptInvitation(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
