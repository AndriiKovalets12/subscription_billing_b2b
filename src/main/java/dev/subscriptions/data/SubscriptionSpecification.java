package dev.subscriptions.data;

import dev.subscriptions.SubscriptionStatus;
import dev.subscriptions.dto.SubscriptionFiltersDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionSpecification {

    public static Specification<SubscriptionEntity> withFilters(Long tenantId, SubscriptionFiltersDto filters){
        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            predicates.add(cb.equal(root.get("status"), SubscriptionStatus.ACTIVE));

            if (filters != null){
                if (filters.startDate() != null){
                    predicates.add(cb.equal(root.get("startDate"), filters.startDate()));
                } else {
                    if (filters.startDateMin() != null){
                        predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), filters.startDateMin()));
                    }
                    if (filters.startDateMax() != null){
                        predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), filters.startDateMax()));
                    }
                }

                if (filters.nextBillingDate() != null){
                    predicates.add(cb.equal(root.get("nextBillingDate"), filters.nextBillingDate()));
                } else {
                    if (filters.nextBillingDateMin() != null){
                        predicates.add(cb.greaterThanOrEqualTo(root.get("nextBillingDate"), filters.nextBillingDateMin()));
                    }
                    if (filters.nextBillingDateMax() != null){
                        predicates.add(cb.lessThanOrEqualTo(root.get("nextBillingDate"), filters.nextBillingDateMax()));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }
}
