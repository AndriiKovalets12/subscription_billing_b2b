package dev.customers;

import dev.customers.dto.CustomerDto;
import dev.customers.dto.SyncCustomerDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integrations")
public class CustomerIntegrationsController {
    private static final Logger log = LoggerFactory.getLogger(CustomerIntegrationsController.class);
    private final CustomerService service;

    public CustomerIntegrationsController(CustomerService service) {
        this.service = service;
    }

    @PutMapping("/customers/{externalCustomerId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<CustomerDto> syncCustomer(
            @PathVariable("externalCustomerId") String externalCustomerId,
            @Valid @RequestBody SyncCustomerDto syncDto) {

        log.info("Called syncCostumer with externalCustomerId={}, syncDto:{}", externalCustomerId, syncDto.toString());
        CustomerDto customerDto = service.sync(externalCustomerId, syncDto);
        return ResponseEntity.ok(customerDto);
    }

    @DeleteMapping("/customers/{externalCustomerId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> deleteCustomerByExternalId(
            @PathVariable("externalCustomerId") String externalCustomerId) {

        log.info("Called deleteCustomer() with externalCustomerId={}", externalCustomerId);

        service.deleteByExternalId(externalCustomerId);

        return ResponseEntity.noContent().build(); // HTTP 204
    }
}
