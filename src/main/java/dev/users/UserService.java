package dev.users;

import dev.security.SecurityUtils;
import dev.tenants.data.TenantEntity;
import dev.tenants.data.TenantRepository;
import dev.users.data.UserEntity;
import dev.users.data.UserRepository;
import dev.users.data.UserSpecification;
import dev.users.dto.CreateUserDto;
import dev.users.dto.FiltersUserDto;
import dev.users.dto.UpdateProfileUserDto;
import dev.users.dto.UserDto;
import dev.users.security.UserRole;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, TenantRepository tenantRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAll(FiltersUserDto filters, Pageable pageable){
        Specification<UserEntity> spec;

        if (SecurityUtils.hasRole("SUPER_ADMIN")){
            spec = UserSpecification.withFiltersForAdmin(filters);
        } else {
            Long currentTenantId = SecurityUtils.getCurrentTenantId();
            spec = UserSpecification.withFilters(currentTenantId, filters);
        }

        Page<UserEntity> userEntities = userRepository.findAll(spec, pageable);

        return userEntities.map(this::mapperToDto);
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        UserEntity user;

        if (SecurityUtils.hasRole("SUPER_ADMIN")){
            user = userRepository
                    .findByIdAndIsActiveTrue(id)
                    .orElseThrow(() -> new EntityNotFoundException("User with id=" + id + " not found."));
        } else {
            Long currentTenantId = SecurityUtils.getCurrentTenantId();
            user = userRepository
                    .findByIdAndTenantIdAndIsActiveTrue(id, currentTenantId)
                    .orElseThrow(() -> new EntityNotFoundException("User with id=" + id + " not found."));
        }

        return mapperToDto(user);
    }

    @Transactional(readOnly = true)
    public boolean exists(String email){
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public UserDto create(@Valid CreateUserDto userToCreate) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        TenantEntity tenant = tenantRepository
                .findById(currentTenantId)
                .orElseThrow(EntityNotFoundException::new);

        if (userRepository.existsByEmail(userToCreate.email())){
            UserEntity createdUser =
                    new UserEntity(
                            userToCreate.firstName(),
                            userToCreate.lastName(),
                            userToCreate.email(),
                            passwordEncoder.encode(userToCreate.rawPassword()),
                            userToCreate.userRole(),
                            tenant
                    );

            // Додай у UserService.create():
            if (userToCreate.userRole() == UserRole.SUPER_ADMIN && !SecurityUtils.hasRole("SUPER_ADMIN")) {
                throw new AccessDeniedException("You do not have permission to assign SUPER_ADMIN role.");
            }

            userRepository.save(createdUser);
            return mapperToDto(createdUser);

        } else {
            throw new EntityExistsException("User with this email already exists");
        }

    }

    @Transactional
    public void registerNewUser(@Valid CreateUserDto userToCreate, Long tenantId){

        TenantEntity tenant = tenantRepository
                .findById(tenantId)
                .orElseThrow(EntityNotFoundException::new);

        if (userRepository.existsByEmail(userToCreate.email())){
            UserEntity createdUser =
                    new UserEntity(
                            userToCreate.firstName(),
                            userToCreate.lastName(),
                            userToCreate.email(),
                            passwordEncoder.encode(userToCreate.rawPassword()),
                            userToCreate.userRole(),
                            tenant
                    );

            userRepository.save(createdUser);
            mapperToDto(createdUser);

        } else {
            throw new EntityExistsException("User with this email already exists");
        }
    }


    @Transactional
    public UserDto updateProfile(Long id, @Valid UpdateProfileUserDto profileToUpdate) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();
        UserEntity user = userRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException("User with id=" + id + " not found."));

        user.updateProfile(
                profileToUpdate.firstName(),
                profileToUpdate.lastName()
        );

        return mapperToDto(user);
    }

    @Transactional
    public void deactivate(Long id) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        UserEntity user = userRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException("User with id=" + id + " not found."));

        user.deactivate();
    }

    private UserDto mapperToDto(UserEntity entity){
        return new UserDto(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getUserRole().toString(),
                entity.getTenant().getId());
    }

}
