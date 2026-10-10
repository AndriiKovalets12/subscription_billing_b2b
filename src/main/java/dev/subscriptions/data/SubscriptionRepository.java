package dev.subscriptions.data;

import dev.subscriptions.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long>, JpaSpecificationExecutor<SubscriptionEntity> {

    List<SubscriptionEntity> findAllByTenantIdAndStatus(Long tenant_id, SubscriptionStatus status);

    Optional<SubscriptionEntity> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT s FROM SubscriptionEntity s JOIN FETCH s.customer JOIN FETCH s.subscriptionPlan JOIN FETCH s.tenant WHERE s.status = :status AND s.nextBillingDate <= :date")
    Page<SubscriptionEntity> findActiveSubscriptionsByBilling(
            @Param("date") OffsetDateTime date,
            @Param("status") SubscriptionStatus status,
            Pageable pageable);

    @Query("SELECT s FROM SubscriptionEntity s JOIN FETCH s.customer WHERE s.status = :status AND s.nextRetryDate <= :date")
    Page<SubscriptionEntity> findPastDueSubscriptionsByBilling(
            @Param("date")OffsetDateTime date,
            @Param("status") SubscriptionStatus status,
            Pageable pageable);

    List<SubscriptionEntity> findAllByCustomerIdAndTenantIdAndStatus(Long customer_id, Long tenant_id, SubscriptionStatus status);

    boolean existsByCustomerIdAndSubscriptionPlanIdAndStatusAndTenantId(@NotNull(message = "Customer's id cannot be empty or null.") @Positive(message = "Customer's id must be greater than zero.") Long aLong, @NotNull(message = "Subscription plan id cannot be empty or null.") @Positive(message = "Subscription plan id must be greater than zero.") Long aLong1, SubscriptionStatus subscriptionStatus, Long currentTenantId);

}