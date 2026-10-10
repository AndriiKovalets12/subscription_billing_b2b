package dev.users.data;

import dev.users.security.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenant_id);

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByIdAndIsActiveTrue(Long id);

    List<UserEntity> findAllByTenantIdAndUserRoleAndIsActiveTrue(Long tenant_id, UserRole userRole);
}
