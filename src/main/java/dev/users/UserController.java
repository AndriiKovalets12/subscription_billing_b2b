package dev.users;

import dev.users.dto.CreateUserDto;
import dev.users.dto.FiltersUserDto;
import dev.users.dto.UpdateProfileUserDto;
import dev.users.dto.UserDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @Valid FiltersUserDto filters,
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.DESC)
            Pageable pageable
    ){

        log.info("Called getAllUsers(FiltersUserDto filters, Pageable pageable) with filters:{}, pageable{}.",
                filters.toString(), pageable.toString());

        Page<UserDto> users = service.getAll(filters, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id){

        log.info("Called getUserById(Long id) with id={}.", id);

        UserDto user = service.getById(id);

        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<UserDto> createNewUser(@Valid @RequestBody CreateUserDto userToCreate){

        log.info("Called createNewUser(CreateUserDto userToCreate) with userToCreate:{}.", userToCreate);

        UserDto createdUser = service.create(userToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PatchMapping("/{id}/profile")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<UserDto> updateUserProfile(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateProfileUserDto profileToUpdate){

        log.info("Called updateUserProfile(Long id, UpdateProfileUserDto profileToUpdate) with id={}, profileToUpdate:{}", id, profileToUpdate);

        UserDto updatedProfile = service.updateProfile(id, profileToUpdate);
        return ResponseEntity.ok(updatedProfile);
    }

    ///Дописать updateEmail(String newEmail) з генерацією листа для підтвердження
    ///Дописать updatePassword(String newPassword)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id){

        log.info("Called deactivateUser(Long id) with id={}", id);

        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }


}
