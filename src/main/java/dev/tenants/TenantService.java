package dev.tenants;

import dev.security.SecurityUtils;
import dev.tenants.data.TenantEntity;
import dev.tenants.data.TenantRepository;
import dev.tenants.data.TenantSpecification;
import dev.tenants.dto.CreateTenantDto;
import dev.tenants.dto.FiltersTenantDto;
import dev.tenants.dto.TenantDto;
import dev.tenants.dto.UpdateTenantNameDto;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public Page<TenantDto> getAll(FiltersTenantDto filters, Pageable pageable) {
        Specification<TenantEntity> spec = TenantSpecification.withFilters(filters);

        Page<TenantEntity> tenantEntities = tenantRepository.findAll(spec, pageable);
        return tenantEntities.map(this::mapperToDto);
    }

    @Transactional(readOnly = true)
    public TenantDto getById(Long id) {

        TenantEntity tenantEntity = tenantRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant with id=" + id + " not found."));

        return mapperToDto(tenantEntity);
    }

    @Transactional(readOnly = true)
    public TenantEntity getTenantBy(Long tenantId){
        return tenantRepository
                .findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant with id=" + tenantId + " not found."));
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name){
        return tenantRepository.existsByName(name);
    }

    @Transactional
    public TenantDto create(CreateTenantDto tenantToCreate) {
        if (!tenantRepository.existsByName(tenantToCreate.name())){

            TenantEntity tenant = new TenantEntity(tenantToCreate.name());
            tenantRepository.save(tenant);

            return mapperToDto(tenant);
        } else {
            throw new EntityExistsException("Tenant with this name already exists.");
        }
    }

    @Transactional
    public TenantDto updateName(Long targetTenantId, UpdateTenantNameDto tenantToUpdate){
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        boolean isSuperAdmin = SecurityUtils.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !currentTenantId.equals(targetTenantId)){
            throw new AccessDeniedException("Users can modify only their own tenants");
        }

        TenantEntity tenant = tenantRepository.findById(targetTenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant with targetTenantId=" + targetTenantId + " not found."));

        String newName = tenantToUpdate.name();

        if (tenant.getName().equals(newName)) {
            return mapperToDto(tenant);
        }

        if (tenantRepository.existsByName(newName)) {
            throw new EntityExistsException("Tenant with the name '" + newName + "' already exists.");
        }

        tenant.updateName(newName);

        return mapperToDto(tenant);
    }

    private TenantDto mapperToDto(TenantEntity entity){
        return new TenantDto(
                entity.getId(),
                entity.getName());
    }
}
