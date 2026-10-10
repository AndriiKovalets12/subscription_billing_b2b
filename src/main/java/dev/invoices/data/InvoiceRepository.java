package dev.invoices.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long>, JpaSpecificationExecutor<InvoiceEntity> {

    InvoiceEntity findByIdempotencyKey(String idempotencyKey);

    Optional<InvoiceEntity> findByIdAndTenantId(Long id, Long currentTenantId);
}
