package dev.subscriptions;

import dev.subscriptions.dto.CreateSubscriptionDto;
import dev.subscriptions.dto.SubscriptionDto;
import dev.subscriptions.dto.SubscriptionFiltersDto;
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
@RequestMapping("api/v1/subscriptions")
public class SubscriptionController {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);
    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_SUPPORT', 'TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<Page<SubscriptionDto>> getAllActiveSubscriptions(
            @Valid SubscriptionFiltersDto filters,
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        log.info("Called getAllActiveSubscriptions().");

        Page<SubscriptionDto> activeSub = service.getAllActive(filters, pageable);
        return ResponseEntity.ok(activeSub);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_SUPPORT', 'TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<SubscriptionDto> getSubscriptionById(@PathVariable Long id){
        log.info("Called getSubscriptionById(Long id) with id={}.", id);

        SubscriptionDto subscription = service.getById(id);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<SubscriptionDto> createSubscription(@Valid @RequestBody CreateSubscriptionDto subscriptionToCreate){

        log.info("Called createSubscription(CreateSubscriptionDto subscriptionToCreate) with subscriptionToCreate:{}", subscriptionToCreate.toString());

        SubscriptionDto createdSubscription = service.create(subscriptionToCreate);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubscription);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<Void> cancelSubscription(@PathVariable Long id){
        log.info("Called cancelSubscription(Long id) with id={}", id);

        service.cancel(id);

        return ResponseEntity.noContent().build();
    }
}
