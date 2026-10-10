package dev.customers;

import dev.customers.data.CustomerEntity;
import dev.customers.data.CustomerRepository;
import dev.customers.data.CustomerSpecification;
import dev.customers.dto.CreateCustomerDto;
import dev.customers.dto.CustomerDto;
import dev.customers.dto.CustomerFiltersDto;
import dev.customers.dto.SyncCustomerDto;
import dev.security.SecurityUtils;
import dev.subscriptions.SubscriptionService;
import dev.tenants.data.TenantEntity;
import dev.tenants.data.TenantRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final TenantRepository tenantRepository;
    private final SubscriptionService subscriptionService;

    public CustomerService(CustomerRepository customerRepository,
                           TenantRepository tenantRepository,
                           SubscriptionService subscriptionService) {
        this.customerRepository = customerRepository;
        this.tenantRepository = tenantRepository;
        this.subscriptionService = subscriptionService;
    }

    public Page<CustomerDto> getAll(CustomerFiltersDto filters, Pageable pageable){
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        Specification<CustomerEntity> spec = CustomerSpecification.withFilters(currentTenantId, filters);

        return pageMapperToDto(customerRepository.findAll(spec, pageable));
    }

    public CustomerDto getById(Long id){
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        CustomerEntity customer = customerRepository.findByIdAndTenantIdAndIsActiveTrue(id, currentTenantId)
                .orElseThrow(()->new EntityNotFoundException("Customer with id=" + id + "not found."));

        return mapperToDto(customer);
    }

    @Transactional
    public CustomerDto create(CreateCustomerDto customerToCreate){
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        TenantEntity tenant = tenantRepository
                .findById(currentTenantId)
                .orElseThrow(()->new EntityNotFoundException("Tenant with id=" + currentTenantId + " not found."));

        CustomerEntity createdCustomer = new CustomerEntity(
                customerToCreate.firstName(),
                customerToCreate.lastName(),
                customerToCreate.email(),
                customerToCreate.customerExternalId(),
                tenant
        );

        if (customerRepository.existsByExternalCustomerIdAndTenantId
                (createdCustomer.getExternalCustomerId(), currentTenantId)) {
            throw new EntityExistsException("Customer with this external ID already exists.");
        }

        customerRepository.save(createdCustomer);
        return mapperToDto(createdCustomer);
    }

    @Transactional
    public CustomerDto sync(String externalCustomerId, SyncCustomerDto syncDto) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        // 1. Шукаємо НЕЗАЛЕЖНО від прапорця isActive, щоб знайти навіть архівного клієнта
        CustomerEntity customer = customerRepository
                .findByExternalCustomerIdAndTenantId(externalCustomerId, currentTenantId)
                .orElse(null);

        TenantEntity tenant = tenantRepository
                .findById(currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant with id=" + currentTenantId + " not found."));

        if (customer == null) {
            // Створюємо нового (isActive = true за замовчуванням)
            customer = new CustomerEntity(
                    syncDto.firstName(),
                    syncDto.lastName(),
                    syncDto.email(),
                    externalCustomerId,
                    tenant
            );
        } else {
            // Якщо клієнт був логічно видалений, але від B2B-партнера знову прийшли дані — реактивуємо його!
            if (!customer.isActive()) {
                customer.activate();
            }
            customer.updateProfile(syncDto.firstName(), syncDto.lastName(), syncDto.email());
        }


        customer = customerRepository.save(customer);
        return mapperToDto(customer);
    }


    @Transactional
    public void deleteByExternalId(String externalCustomerId) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        CustomerEntity customer = customerRepository
                .findByExternalCustomerIdAndTenantId(externalCustomerId, currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Customer not found with tenantId=" + currentTenantId + " and externalCustomerId=" + externalCustomerId));

        Long customerId = customer.getId();

        customer.deactivate();
        subscriptionService.cancelAllSubscriptions(customerId);

        customerRepository.save(customer);
    }

    @Transactional
    public void deleteById(Long customerId) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        CustomerEntity customer = customerRepository.findByIdAndTenantIdAndIsActiveTrue(customerId, currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found."));

        customer.deactivate();
        subscriptionService.cancelAllSubscriptions(customerId);

        customerRepository.save(customer);
    }

    private CustomerDto mapperToDto(CustomerEntity entity){
        return new CustomerDto(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getExternalCustomerId(),
                entity.getTenant().getId());
    }

    private Page<CustomerDto> pageMapperToDto(Page<CustomerEntity> entities){
        return entities.map(this::mapperToDto);
    }
}

