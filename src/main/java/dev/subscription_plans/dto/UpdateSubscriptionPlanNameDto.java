package dev.subscription_plans.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSubscriptionPlanNameDto(
        @NotBlank(message = "The new name cannot be empty.")
        String name
        
) {
    @Override
    public String toString() {
        return "{name=" + name + "}";
    }
}
