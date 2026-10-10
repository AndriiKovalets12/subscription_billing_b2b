package dev.invoices;

import dev.invoices.dto.CreateInvoiceDto;
import dev.invoices.dto.InvoiceDto;
import dev.invoices.dto.InvoiceFiltersDto;
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
@RequestMapping("api/v1/invoices")
public class InvoiceController {
    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);
    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_SUPPORT', 'TENANT_OWNER')")
    public ResponseEntity<Page<InvoiceDto>> getAllInvoices(
            @Valid InvoiceFiltersDto filters,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        log.info("Called getAllInvoices(filters, pageable) with filters:{}, pageable:{}",
                filters.toString(), pageable.toString());

        Page<InvoiceDto> invoices = service.getAll(filters, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_SUPPORT', 'TENANT_OWNER')")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id){
        log.info("Called getInvoiceById(Long id) with id={}", id);

        InvoiceDto invoice = service.getById(id);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_OWNER')")
    public ResponseEntity<InvoiceDto> createInvoice(@Valid @RequestBody CreateInvoiceDto invoiceToCreate){
        log.info("Called createInvoice(CreateInvoiceDto invoiceToCreate) with invoiceToCreate:{}", invoiceToCreate.toString());

        InvoiceDto createdInvoice = service.create(invoiceToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInvoice);
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("hasRole('TENANT_SUPPORT')")
    public ResponseEntity<InvoiceDto> manualPaymentRetry(@PathVariable("id") Long invoiceId){
        log.info("Called retryPayment(Long invoiceId) with invoiceId={}", invoiceId);

        InvoiceDto retriedInvoice = service.retry(invoiceId);

        return ResponseEntity.ok(retriedInvoice);
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<InvoiceDto> voidInvoice(@PathVariable("id") Long invoiceId){
        log.info("Called voidInvoice(Long invoiceId) with invoiceId={}", invoiceId);

        InvoiceDto voidedInvoice = service.voidInvoice(invoiceId);

        return ResponseEntity.ok(voidedInvoice);
    }
}
