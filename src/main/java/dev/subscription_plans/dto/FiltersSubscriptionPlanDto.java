package dev.subscription_plans.dto;

import dev.subscription_plans.BillingCycle;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record FiltersSubscriptionPlanDto(
        String name,

        @Positive(message = "Cost must be greater than zero.")
        BigDecimal cost,

        @PositiveOrZero(message = "Min cost cannot be negative.")
        BigDecimal minCost,

        @Positive(message = "Max cost must be greater than zero.")
        BigDecimal maxCost,

        BillingCycle duration
) {
        @AssertTrue(message = "minCost cannot be greater than maxCost.")
        public boolean isCostRangeValid() {
                if (minCost != null && maxCost != null) {
                        return minCost.compareTo(maxCost) <= 0;
                }
                return true;
        }
}
