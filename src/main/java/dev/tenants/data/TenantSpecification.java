package dev.tenants.data;

import dev.tenants.dto.FiltersTenantDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class TenantSpecification {
    public static Specification<TenantEntity> withFilters(FiltersTenantDto filters){

        return ((root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));

            if (filters != null){
                if (StringUtils.hasText(filters.name())) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + filters.name().toLowerCase() + "%"));
                }
            }

            return cb.and(predicates);
        });
    }
}
