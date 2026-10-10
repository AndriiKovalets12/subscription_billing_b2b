package dev.security;

import dev.users.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

public class SecurityUtils {

    public static Long getCurrentTenantId() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser user) {
            return user.getTenantId();
        }
        throw new IllegalStateException("User is not authenticated or tenant context is missing");
    }

    public static boolean hasRole(String role){
        Authentication auth = getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser user) {
            return user.getRole().toString().equals(role);
        }
        throw new IllegalStateException("User is not authenticated or tenant context is missing");
    }

    private static Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Component("tenantSecurity")
    static class TenantSecurity {
        public boolean isCurrentTenant(Long id){
            return Objects.equals(getCurrentTenantId(), id);
        }
    }
    @Component("userSecurity")
    static class UserSecurity{
        public boolean isCurrentUser(Long id){
            Authentication auth = getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof SecurityUser user) {
                return user.getUserId().equals(id);
            }
            throw new IllegalStateException("User is not authenticated or tenant context is missing");
        }
    }
}