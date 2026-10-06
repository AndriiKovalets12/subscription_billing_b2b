package dev.billing_engine.dto;


import dev.invoices.InvoiceStatus;

public record BillingResult(
        Long subscriptionId,
        Long invoiceId,
        InvoiceStatus status,
        String message
) {
}
