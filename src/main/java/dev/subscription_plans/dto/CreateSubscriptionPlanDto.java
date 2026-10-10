package dev.subscription_plans.dto;

import dev.subscription_plans.BillingCycle;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateSubscriptionPlanDto(
        @NotBlank(message = "Name cannot be empty.")
        @Size(max = 25, message = "Name is too long.")
        String name,

        @NotNull(message = "Price cannot be empty or null.")
        @Positive(message = "Price must be greater than zero.")
        BigDecimal cost,

        @NotNull(message = "Duration cannot be empty or null.")
        BillingCycle duration

) {
        @Override
        public String toString() {
                return "{" + name +
                        ", cost=" + cost.toString() +
                        ", duration=" + duration.toString() +
                        " }";
        }
}
