package dev.invoices.dto;

import dev.invoices.InvoiceStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record InvoiceFiltersDto(
        InvoiceStatus status,

        @Positive(message = "Amount must be greater than zero.")
        BigDecimal amount,

        @PositiveOrZero(message = "Min amount cannot be negative.")
        BigDecimal minAmount,

        @Positive(message = "Max amount must be greater than zero.")
        BigDecimal maxAmount,

        OffsetDateTime createdAfter,
        OffsetDateTime createdBefore,

        OffsetDateTime billingPeriodStartMin,
        OffsetDateTime billingPeriodStartMax,

        OffsetDateTime billingPeriodEndMin,
        OffsetDateTime billingPeriodEndMax
) {
    @Override
    public String toString() {
        return "{" +
                "status=" + status +
                ", amount=" + amount +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", createdAfter=" + createdAfter +
                ", createdBefore=" + createdBefore +
                ", billingPeriodStartMin=" + billingPeriodStartMin +
                ", billingPeriodStartMax=" + billingPeriodStartMax +
                ", billingPeriodEndMin=" + billingPeriodEndMin +
                ", billingPeriodEndMax=" + billingPeriodEndMax +
                '}';
    }

    @AssertTrue(message = "minAmount cannot be greater than maxAmount.")
    public boolean isAmountRangeValid() {
        if (minAmount != null && maxAmount != null) {
            return minAmount.compareTo(maxAmount) <= 0;
        }
        return true;
    }
}
