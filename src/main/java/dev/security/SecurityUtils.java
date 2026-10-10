package dev.security;

import dev.users.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getCurrentTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser user) {
            return user.getTenantId();
        }
        throw new IllegalStateException("User is not authenticated or tenant context is missing");
    }

    public static boolean hasRole(String role){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser user) {
            return user.getRole().toString().equals(role);
        }
        throw new IllegalStateException("User is not authenticated or tenant context is missing");
    }
}