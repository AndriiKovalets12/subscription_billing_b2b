package dev.invoices.data;

import dev.invoices.dto.InvoiceFiltersDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InvoiceSpecification {

    public static Specification<InvoiceEntity> withFilters(Long tenantId, InvoiceFiltersDto filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));

            if (filters != null) {
                // Status criteria
                if (filters.status() != null) {
                    predicates.add(cb.equal(root.get("status"), filters.status()));
                }

                // Amount criteria
                if (filters.amount() != null) {
                    predicates.add(cb.equal(root.get("amount"), filters.amount()));
                } else {
                    if (filters.minAmount() != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filters.minAmount()));
                    }
                    if (filters.maxAmount() != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filters.maxAmount()));
                    }
                }

                // CreatedAt criteria
                if (filters.createdAfter() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filters.createdAfter()));
                }
                if (filters.createdBefore() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filters.createdBefore()));
                }

                // BillingPeriodStart criteria
                if (filters.billingPeriodStartMin() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("billingPeriodStart"), filters.billingPeriodStartMin()));
                }
                if (filters.billingPeriodStartMax() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("billingPeriodStart"), filters.billingPeriodStartMax()));
                }

                // BillingPeriodEnd criteria
                if (filters.billingPeriodEndMin() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("billingPeriodEnd"), filters.billingPeriodEndMin()));
                }
                if (filters.billingPeriodEndMax() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("billingPeriodEnd"), filters.billingPeriodEndMax()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}