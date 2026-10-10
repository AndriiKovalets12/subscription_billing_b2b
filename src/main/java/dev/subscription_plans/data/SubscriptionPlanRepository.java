package dev.subscription_plans.data;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanEntity, Long>, JpaSpecificationExecutor<SubscriptionPlanEntity> {
    Optional<SubscriptionPlanEntity> findByIdAndTenantIdAndIsActiveTrue(Long id, Long tenantId);

    Optional<SubscriptionPlanEntity> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByNameAndIdAndTenantId(String name, Long id, Long tenant_id);
}
