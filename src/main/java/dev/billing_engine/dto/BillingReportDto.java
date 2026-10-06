package dev.billing_engine.dto;

import java.util.List;

public record BillingReportDto(
        Long totalProcessed,
        Long totalSuccessful,
        Long failedPayments,
        List<BillingResult> details
) {
}
