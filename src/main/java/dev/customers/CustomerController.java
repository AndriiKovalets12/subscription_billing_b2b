package dev.customers;

import dev.customers.dto.CreateCustomerDto;
import dev.customers.dto.CustomerDto;
import dev.customers.dto.CustomerFiltersDto;
import jakarta.validation.Valid;
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
@RequestMapping("api/v1/customers")
public class CustomerController {
    private final CustomerService service;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CustomerController.class);

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_SUPPORT', 'TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<Page<CustomerDto>> getAllCustomers(
            CustomerFiltersDto filters,
            @PageableDefault(size = 20, sort = "email", direction = Sort.Direction.ASC)
            Pageable pageable
    ){

        log.info("Called getAllSubPlans(filters, pageable) with filters:{}, pageable:{}",
                filters.toString(), pageable.toString());

        Page<CustomerDto> allCustomers = service.getAll(filters, pageable);
        return ResponseEntity.ok(allCustomers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_SUPPORT', 'TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id){

        log.info("Called getCustomerById(Long id) with id={}", id);

        CustomerDto customerDto = service.getById(id);
        return ResponseEntity.ok(customerDto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CreateCustomerDto customerToCreate){

        log.info("Called createCustomer(CreateCustomerDto customerToCreate) with customerToCreate={}", customerToCreate);

        CustomerDto customerDto = service.create(customerToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<Void> deleteCustomerById(@PathVariable("id") Long id){

        log.info("Called deleteCustomerById(Long id) with id={}", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
