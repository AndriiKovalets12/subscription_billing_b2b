package dev.subscription_plans.data;

import dev.subscription_plans.dto.FiltersSubscriptionPlanDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionPlanSpecification {
    public static Specification<SubscriptionPlanEntity> withFilters(Long tenantId, FiltersSubscriptionPlanDto filters){

        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            predicates.add(cb.isTrue(root.get("isActive")));

            if (StringUtils.hasText(filters.name())){
                predicates.add(cb.equal(root.get("name"), filters.name()));
            }

            if (filters.cost() != null){
                predicates.add(cb.equal(root.get("cost"), filters.cost()));
            } else {
                if (filters.minCost() != null){
                    predicates.add(cb.greaterThanOrEqualTo(root.get("cost"), filters.minCost()));
                }
                if (filters.maxCost() != null){
                    predicates.add(cb.lessThanOrEqualTo(root.get("cost"), filters.maxCost()));
                }
            }

            if (filters.duration() != null){
                predicates.add(cb.equal(root.get("duration"), filters.duration()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));

        } );
    }
}
