package dev.subscription_plans.dto;

import dev.subscription_plans.BillingCycle;
import java.math.BigDecimal;

public record SubscriptionPlanDto(
        long id,
        String name,
        BigDecimal cost,
        BillingCycle duration,
        Long tenantId,
        boolean isActive
) {
    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cost=" + cost +
                ", duration=" + duration +
                ", tenantId=" + tenantId +
                ", isActive=" + isActive +
                '}';
    }
}
