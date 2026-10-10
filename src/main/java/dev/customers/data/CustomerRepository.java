package dev.customers.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long>, JpaSpecificationExecutor<CustomerEntity> {
    Optional<CustomerEntity> findByExternalCustomerIdAndTenantId(String externalCustomerId, Long tenantId);

    Optional<CustomerEntity> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    Page<CustomerEntity> findAllByTenantIdAndIsActiveTrue(Long tenant_id,
                                                          Pageable pageable);

    boolean existsByExternalCustomerIdAndTenantId(String externalCustomerId, Long currentTenantId);
}
