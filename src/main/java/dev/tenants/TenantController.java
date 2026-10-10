package dev.tenants;

import dev.tenants.dto.CreateTenantDto;
import dev.tenants.dto.FiltersTenantDto;
import dev.tenants.dto.TenantDto;
import dev.tenants.dto.UpdateTenantNameDto;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {
    private static final Logger log = LoggerFactory.getLogger(TenantController.class);
    private final TenantService service;

    public TenantController(TenantService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<TenantDto>> getAllTenants(
            FiltersTenantDto filters,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.DESC)
            Pageable pageable
    ){

        log.info("Called getAllTenants().");

        Page<TenantDto> tenants = service.getAll(filters, pageable);
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER') and @tenantSecurity.isCurrentTenant(#id))")
    public ResponseEntity<TenantDto> getTenantById(@PathVariable Long id){

        log.info("Called getTenantById(Long id) with id={}.", id);

        TenantDto tenant = service.getById(id);
        return ResponseEntity.ok(tenant);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('CREATE_TENANTS')")
    public ResponseEntity<TenantDto> createTenant(@Valid @RequestBody CreateTenantDto tenantToCreate){

        log.info("Called createTenant(CreateTenantDto tenantToCreate) with tenantToCreate:{}", tenantToCreate.toString());

        TenantDto createdTenant = service.create(tenantToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER') and @tenantSecurity.isCurrentTenant(#id))")
    public ResponseEntity<TenantDto> updateTenantName(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateTenantNameDto tenantToUpdate){

        log.info("Called updateTenantName(CreateTenantDto tenantToUpdate) with tenantToUpdate:{}", tenantToUpdate.toString());

        TenantDto updatedTenant = service.updateName(id, tenantToUpdate);
        return ResponseEntity.ok(updatedTenant);
    }
}
