package dev.users.data;

import dev.users.dto.FiltersUserDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<UserEntity> withFilters(Long tenantId, FiltersUserDto filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            predicates.add(cb.isTrue(root.get("isActive")));

            // 2. Уникнення N+1 для сторінкових запитів
            if (Long.class != query.getResultType()) {
                root.fetch("tenant", JoinType.LEFT);
            }

            return combinePredicates(filters, root, cb, predicates);
        };
    }

    public static Specification<UserEntity> withFiltersForAdmin(FiltersUserDto filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("isActive")));

            if (Long.class != query.getResultType()) {
                root.fetch("tenant", JoinType.LEFT);
            }

            return combinePredicates(filters, root, cb, predicates);
        };
    }

    private static Predicate combinePredicates(FiltersUserDto filters,
                                               Root<UserEntity> root,
                                               CriteriaBuilder cb,
                                               List<Predicate> predicates) {
        if (filters != null) {
            if (StringUtils.hasText(filters.firstName())) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + filters.firstName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filters.lastName())) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + filters.lastName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filters.email())) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + filters.email().toLowerCase() + "%"));
            }

            if (filters.userRole() != null) {
                predicates.add(cb.equal(root.get("userRole"), filters.userRole()));
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}