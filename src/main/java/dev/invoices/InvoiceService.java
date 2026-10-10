package dev.invoices;

import dev.invoices.data.InvoiceEntity;
import dev.invoices.data.InvoiceRepository;
import dev.invoices.data.InvoiceSpecification;
import dev.invoices.dto.CreateInvoiceDto;
import dev.invoices.dto.InvoiceDto;
import dev.invoices.dto.InvoiceFiltersDto;
import dev.payments.PaymentGateway;
import dev.security.SecurityUtils;
import dev.subscriptions.data.SubscriptionEntity;
import dev.subscriptions.data.SubscriptionRepository;
import dev.tenants.data.TenantEntity;
import dev.tenants.data.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    private final PaymentGateway paymentGateway;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          SubscriptionRepository subscriptionRepository,
                          TenantRepository tenantRepository,
                          PaymentGateway paymentGateway) {
        this.invoiceRepository = invoiceRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.tenantRepository = tenantRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional(readOnly = true)
    public Page<InvoiceDto> getAll(InvoiceFiltersDto filters, Pageable pageable) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();
        Specification<InvoiceEntity> spec = InvoiceSpecification.withFilters(currentTenantId, filters);

        return invoiceRepository.findAll(spec, pageable).map(this::mapperToDto);
    }

    @Transactional(readOnly = true)
    public InvoiceDto getById(Long id) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();
        InvoiceEntity invoice = invoiceRepository.findByIdAndTenantId(id, currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice with id=" + id + " not found."));

        return mapperToDto(invoice);
    }

    // Метод для REST API
    @Transactional
    public InvoiceDto create(@Valid CreateInvoiceDto invoiceToCreate) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();
        InvoiceEntity entity = internalCreate(invoiceToCreate, currentTenantId);
        return mapperToDto(entity);
    }

    // Метод для внутрішнього BillingWorker (не залежить від SecurityContext)
    @Transactional
    public InvoiceEntity createAndReturnEntity(@Valid CreateInvoiceDto invoiceToCreate, Long tenantId) {
        return internalCreate(invoiceToCreate, tenantId);
    }

    private InvoiceEntity internalCreate(CreateInvoiceDto dto, Long tenantId) {
        try {
            TenantEntity tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Tenant with id=" + tenantId + " not found."));

            SubscriptionEntity subscription = subscriptionRepository.findByIdAndTenantId(dto.subscriptionId(), tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Subscription not found for this tenant."));

            InvoiceEntity createdInvoice = new InvoiceEntity(
                    subscription,
                    dto.amount(),
                    dto.status(),
                    dto.billingPeriodStart(),
                    dto.billingPeriodEnd(),
                    1L,
                    dto.idempotencyKey(),
                    tenant
            );

            return invoiceRepository.save(createdInvoice);
        } catch (DataIntegrityViolationException ex) {
            return invoiceRepository.findByIdempotencyKey(dto.idempotencyKey());
        }
    }

    @Transactional
    public InvoiceDto retry(Long invoiceId) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        InvoiceEntity invoice = invoiceRepository.findByIdAndTenantId(invoiceId, currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice with id=" + invoiceId + " not found."));

        if (invoice.getStatus() != InvoiceStatus.FAILED) {
            throw new IllegalStateException("Only invoices with FAILED status can be retried.");
        }

        SubscriptionEntity subscription = invoice.getSubscription();

        if (paymentGateway.processTransaction()) {
            invoice.setStatus(InvoiceStatus.PAID);
            subscription.activate();
            subscription.resetPaymentAttempt();
            subscription.setNextBillingDate(invoice.getBillingPeriodEnd());

            return mapperToDto(invoice);
        } else {
            throw new IllegalStateException("Payment retry failed. Please check customer's payment method or try again later.");
        }
    }

    @Transactional
    public InvoiceDto voidInvoice(Long invoiceId) {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();

        InvoiceEntity invoice = invoiceRepository.findByIdAndTenantId(invoiceId, currentTenantId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice with id=" + invoiceId + " not found."));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice with status PAID cannot be voided. Use REFUND instead.");
        }

        if (invoice.getStatus() == InvoiceStatus.VOID) {
            throw new IllegalStateException("Invoice is already voided.");
        }

        invoice.setStatus(InvoiceStatus.VOID);
        return mapperToDto(invoice);
    }

    private InvoiceDto mapperToDto(InvoiceEntity entity) {
        return new InvoiceDto(
                entity.getSubscription().getId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getBillingPeriodStart(),
                entity.getBillingPeriodEnd(),
                entity.getTenant().getId()
        );
    }
}