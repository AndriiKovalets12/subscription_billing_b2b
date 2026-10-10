package dev.tenants.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<TenantEntity, Long>, JpaSpecificationExecutor<TenantEntity> {
    boolean existsByName(String name);

    Optional<TenantEntity> findByName(String name);
}
