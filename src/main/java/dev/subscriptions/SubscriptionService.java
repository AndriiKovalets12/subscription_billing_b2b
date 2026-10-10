package dev.subscriptions;

import dev.customers.data.CustomerEntity;
import dev.customers.data.CustomerRepository;
import dev.security.SecurityUtils;
import dev.subscription_plans.data.SubscriptionPlanEntity;
import dev.subscription_plans.data.SubscriptionPlanRepository;
import dev.subscriptions.data.SubscriptionEntity;
import dev.subscriptions.data.SubscriptionRepository;
import dev.subscriptions.data.SubscriptionSpecification;
import dev.subscriptions.dto.CreateSubscriptionDto;
import dev.subscriptions.dto.SubscriptionDto;
import dev.subscriptions.dto.SubscriptionFiltersDto;
import dev.tenants.data.TenantEntity;
import dev.tenants.data.TenantRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CustomerRepository customerRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               TenantRepository tenantRepository,
                               SubscriptionPlanRepository subscriptionPlanRepository,
                               CustomerRepository customerRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.tenantRepository = tenantRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionDto> getAllActive(SubscriptionFiltersDto filters, Pageable pageable) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();
        Specification<SubscriptionEntity> spec = SubscriptionSpecification.withFilters(currentTenantId, filters);

        return subscriptionRepository.findAll(spec, pageable).map(this::mapperToDto);
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getById(Long id) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        SubscriptionEntity entity = subscriptionRepository
                .findByIdAndTenantId(id, currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException("Subscriptions with id=" + id + " not found."));

        return mapperToDto(entity);
    }

    @Transactional
    public SubscriptionDto create(@Valid CreateSubscriptionDto subscriptionToCreate) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        TenantEntity tenant = tenantRepository
                .findById(currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found."));

        CustomerEntity customer = customerRepository
                .findByIdAndTenantIdAndIsActiveTrue(subscriptionToCreate.customerId(), currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Customer not found or access denied for this tenant."));

        SubscriptionPlanEntity subscriptionPlan = subscriptionPlanRepository
                .findByIdAndTenantIdAndIsActiveTrue(subscriptionToCreate.subscriptionPlanId(), currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Subscription plan not found or access denied for this tenant."));

        SubscriptionEntity createdSubscription = new SubscriptionEntity(
                subscriptionPlan,
                customer,
                subscriptionToCreate.nextBillingDate(),
                tenant);

        boolean alreadySubscribed = subscriptionRepository.existsByCustomerIdAndSubscriptionPlanIdAndStatusAndTenantId(
                subscriptionToCreate.customerId(),
                subscriptionToCreate.subscriptionPlanId(),
                SubscriptionStatus.ACTIVE,
                currentTenantId
        );

        if (alreadySubscribed) {
            throw new EntityExistsException("Customer already has an active subscription to this plan.");
        }

        subscriptionRepository.save(createdSubscription);
        return mapperToDto(createdSubscription);
    }

    @Transactional
    public void cancel(Long id) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        SubscriptionEntity entity = subscriptionRepository
                .findByIdAndTenantId(id, currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Subscription with id=" + id + " not found or access denied for tenantId=" + currentTenantId));

        if (entity.getStatus().equals(SubscriptionStatus.CANCELED)) {
            throw new IllegalStateException("Subscription is already canceled.");
        }

        entity.cancel();
    }

    @Transactional
    public void cancelAllSubscriptions(Long customerId){
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        List<SubscriptionEntity> activeSubs = subscriptionRepository
                .findAllByCustomerIdAndTenantIdAndStatus(customerId, currentTenantId, SubscriptionStatus.ACTIVE);

        activeSubs.forEach(SubscriptionEntity::cancel);

        List<SubscriptionEntity> pastDueSubs = subscriptionRepository
                .findAllByCustomerIdAndTenantIdAndStatus(customerId, currentTenantId, SubscriptionStatus.PAST_DUE);

        pastDueSubs.forEach(SubscriptionEntity::cancel);

    }

    private SubscriptionDto mapperToDto(SubscriptionEntity entity){
        return new SubscriptionDto(
                entity.getId(),
                entity.getSubscriptionPlan().getId(),
                entity.getCustomer().getId(),
                entity.getStartDate(),
                entity.getNextBillingDate(),
                entity.getStatus(),
                entity.getTenant().getId());
    }

}
