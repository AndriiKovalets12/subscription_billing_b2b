package dev.subscriptions.dto;

import jakarta.validation.constraints.AssertTrue;

import java.time.OffsetDateTime;

public record SubscriptionFiltersDto(

        OffsetDateTime startDate,
        OffsetDateTime startDateMin,
        OffsetDateTime startDateMax,

        OffsetDateTime nextBillingDate,
        OffsetDateTime nextBillingDateMin,
        OffsetDateTime nextBillingDateMax
) {
    @Override
    public String toString() {
        return "{" +
                ", startDate=" + startDate +
                ", startDateMin=" + startDateMin +
                ", startDateMax=" + startDateMax +
                ", nextBillingDate=" + nextBillingDate +
                ", nextBillingDateMin=" + nextBillingDateMin +
                ", nextBillingDateMax=" + nextBillingDateMax +
                '}';
    }

    @AssertTrue(message = "startDateMin must be earlier than startDateMax")
    public boolean checkStartDate(){
        if (startDateMin != null && startDateMax != null){
            return startDateMin.isBefore(startDateMax);
        }
        return true;
    }

    @AssertTrue(message = "nextBillingDateMin must be earlier than nextBillingDateMax")
    public boolean checkNextBillingDate(){
        if (nextBillingDateMin != null && nextBillingDateMax != null){
            return nextBillingDateMin.isBefore(nextBillingDateMax);
        }
        return true;
    }
}
