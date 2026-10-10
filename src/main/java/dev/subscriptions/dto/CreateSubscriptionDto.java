package dev.subscriptions.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public record CreateSubscriptionDto(
        @NotNull(message = "Subscription plan id cannot be empty or null.")
        @Positive(message = "Subscription plan id must be greater than zero.")
        Long subscriptionPlanId,

        @NotNull(message = "Customer's id cannot be empty or null.")
        @Positive(message = "Customer's id must be greater than zero.")
        Long customerId,

        @Future
        OffsetDateTime nextBillingDate
) {
    @Override
    public String toString() {
        return "{" +
                "subscriptionPlanId=" + subscriptionPlanId +
                ", customerId=" + customerId +
                ", nextBillingDate=" + nextBillingDate +
                '}';
    }
}
