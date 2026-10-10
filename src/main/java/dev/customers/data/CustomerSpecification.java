package dev.customers.data;

import dev.customers.dto.CustomerFiltersDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {
    public static Specification<CustomerEntity> withFilters(Long tenantId, CustomerFiltersDto filters){
        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            predicates.add(cb.isTrue(root.get("isActive")));

            if (filters != null){
                if (StringUtils.hasText(filters.email())){
                    predicates.add(cb.equal(cb.lower(root.get("email")), filters.email().toLowerCase()));
                }

                if (StringUtils.hasText(filters.firstName())){
                    predicates.add(cb.equal(cb.lower(root.get("firstName")), filters.firstName().toLowerCase()));
                }

                if (StringUtils.hasText(filters.lastName())){
                    predicates.add(cb.equal(cb.lower(root.get("lastName")), filters.lastName().toLowerCase()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }
        );
    }
}
