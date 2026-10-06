package dev.users;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum UserRole {

    TENANT_SUPPORT(Set.of(
            UserAuthority.READ_CUSTOMERS,
            UserAuthority.READ_INVOICES)
    ),

    SUPER_ADMIN(Set.of(
            UserAuthority.START_BILLING,
            UserAuthority.CREATE_TENANTS,
            UserAuthority.DELETE_TENANTS)
    ),

    TENANT_ADMIN(Set.of(
            UserAuthority.CREATE_PLAN,
            UserAuthority.DELETE_PLAN,
            UserAuthority.CREATE_SUBSCRIPTIONS,
            UserAuthority.CANCEL_SUBSCRIPTIONS
    )),
    ;

    private final Set<UserAuthority> permissions;

    UserRole(Set<UserAuthority> permissions) {
        this.permissions = permissions;
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .collect(Collectors.toList());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }


}
