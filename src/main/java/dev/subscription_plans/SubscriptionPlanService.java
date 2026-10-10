package dev.subscription_plans;

import dev.security.SecurityUtils;
import dev.subscription_plans.data.SubscriptionPlanEntity;
import dev.subscription_plans.data.SubscriptionPlanRepository;
import dev.subscription_plans.data.SubscriptionPlanSpecification;
import dev.subscription_plans.dto.CreateSubscriptionPlanDto;
import dev.subscription_plans.dto.SubscriptionPlanDto;
import dev.subscription_plans.dto.FiltersSubscriptionPlanDto;
import dev.subscription_plans.dto.UpdateSubscriptionPlanNameDto;
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


@Service
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final TenantRepository tenantRepository;

    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository, TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionPlanDto> getAll(FiltersSubscriptionPlanDto filters, Pageable pageable){
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        Specification<SubscriptionPlanEntity> spec = SubscriptionPlanSpecification.withFilters(currentTenantId, filters);

        return subscriptionPlanRepository.findAll(spec, pageable).map(this::mapperToDto);
    }

    @Transactional(readOnly = true)
    public SubscriptionPlanDto getById(Long id){
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        SubscriptionPlanEntity entity = subscriptionPlanRepository
                .findByIdAndTenantId(id, currentTenantId)
                .orElseThrow(EntityNotFoundException::new);

        return mapperToDto(entity);
    }

    @Transactional
    public SubscriptionPlanDto create(@Valid CreateSubscriptionPlanDto subPlanToCreate){
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        TenantEntity tenant = tenantRepository
                .findById(currentTenantId)
                .orElseThrow(EntityNotFoundException::new);

        SubscriptionPlanEntity createdSubPlan =
                new SubscriptionPlanEntity(
                        subPlanToCreate.name(),
                        subPlanToCreate.cost(),
                        subPlanToCreate.duration(),
                        tenant,
                        true
                );

        if (subscriptionPlanRepository.existsByNameAndIdAndTenantId(createdSubPlan.getName(), createdSubPlan.getId(), currentTenantId)){
            throw new EntityExistsException("Subscription plan with name:" + createdSubPlan.getName() + " already exists.");
        }

        subscriptionPlanRepository.save(createdSubPlan);
        return mapperToDto(createdSubPlan);
    }

    @Transactional
    public SubscriptionPlanDto updateName(Long id, @Valid UpdateSubscriptionPlanNameDto subPlanToUpdate){
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        SubscriptionPlanEntity entity = subscriptionPlanRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, currentTenantId)
                .orElseThrow(EntityNotFoundException::new);

        entity.updateName(subPlanToUpdate.name());
        return mapperToDto(entity);
    }

    @Transactional
    public void delete(Long id){
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        SubscriptionPlanEntity entity = subscriptionPlanRepository
                .findByIdAndTenantIdAndIsActiveTrue(id, currentTenantId)
                .orElseThrow(EntityNotFoundException::new);

        entity.archive();
    }


    private SubscriptionPlanDto mapperToDto(SubscriptionPlanEntity entity){
        return new SubscriptionPlanDto(
                entity.getId(),
                entity.getName(),
                entity.getCost(),
                entity.getDuration(),
                entity.getTenant().getId(),
                entity.isActive());
    }
}
