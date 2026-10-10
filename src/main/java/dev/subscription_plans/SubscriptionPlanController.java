package dev.subscription_plans;

import dev.subscription_plans.dto.CreateSubscriptionPlanDto;
import dev.subscription_plans.dto.SubscriptionPlanDto;
import dev.subscription_plans.dto.FiltersSubscriptionPlanDto;
import dev.subscription_plans.dto.UpdateSubscriptionPlanNameDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/plans")
public class SubscriptionPlanController {
    private final SubscriptionPlanService service;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SubscriptionPlanController.class);

    public SubscriptionPlanController(SubscriptionPlanService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_SUPPORT', 'TENANT_OWNER')")
    public ResponseEntity<Page<SubscriptionPlanDto>> getAllSubPlans(
            @Valid FiltersSubscriptionPlanDto filters,
            @PageableDefault(size = 20, sort = "cost", direction = Sort.Direction.DESC)
            Pageable pageable
    ){

        log.info("Called getAllSubPlans().");

        Page<SubscriptionPlanDto> planDtoList = service.getAll(filters, pageable);
        return ResponseEntity.ok(planDtoList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_SUPPORT', 'TENANT_OWNER')")
    public ResponseEntity<SubscriptionPlanDto> getSubPlanById(@PathVariable Long id){

        log.info("Called getSubPlanById(Long id) with id={}", id);

        SubscriptionPlanDto subPlanDto = service.getById(id);
        return ResponseEntity.ok(subPlanDto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<SubscriptionPlanDto> createSubPlan(
            @Valid @RequestBody CreateSubscriptionPlanDto subPlanToCreate){

        log.info("Called createSubPlan(CreateSubscriptionPlanDto subPlanToCreate) with " +
                "subPlanToCreate:{}", subPlanToCreate.toString());

        SubscriptionPlanDto newSubPlan = service.create(subPlanToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSubPlan);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<SubscriptionPlanDto> updateSubPlanName(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubscriptionPlanNameDto subPlanToUpdate){

        log.info("Called updateSubPlan(Long id, UpdateSubscriptionPlanDto subPlanToUpdate) with " +
                "id={}, subPlanToUpdate:{}", id, subPlanToUpdate.toString());

        SubscriptionPlanDto updatedSubPlan = service.updateName(id, subPlanToUpdate);
        return ResponseEntity.ok(updatedSubPlan);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<Void> deleteSubPlan(@PathVariable Long id){

        log.info("Called deleteSubPlan(Long id) with id={}", id);

        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
