package dev.invoices.dto;

import dev.invoices.InvoiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateInvoiceDto(
        @NotNull(message = "Subscription's id cannot be empty or null.")
        @Positive(message = "Subscription's id must be greater than zero.")
        Long subscriptionId,

        @NotNull(message = "Amount cannot be empty or null.")
        @Positive(message = "Amount must be greater than zero.")
        BigDecimal amount,

        @NotNull(message = "Status cannot be empty or null.")
        InvoiceStatus status,

        OffsetDateTime billingPeriodStart,

        @NotNull
        OffsetDateTime billingPeriodEnd,

        @NotBlank
        String idempotencyKey

) {
    @Override
    public String toString() {
        return "{" +
                "subscriptionId=" + subscriptionId +
                ", amount=" + amount +
                ", status=" + status +
                ", billingPeriodStart=" + billingPeriodStart +
                ", billingPeriodEnd=" + billingPeriodEnd +
                ", idempotencyKey='" + idempotencyKey + '\'' +
                '}';
    }
}
