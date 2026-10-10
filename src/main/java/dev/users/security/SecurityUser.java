package dev.users.security;

import dev.users.data.UserEntity;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {
    @Getter
    private final Long tenantId;
    private final String username;
    private final String password;
    private final boolean isActive;
    @Getter
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(UserEntity user, UserRole role) {
        this.tenantId = user.getTenant().getId();
        this.username = user.getEmail();
        this.password = user.getHashPassword();
        this.isActive = user.isActive();
        this.authorities = List.copyOf(user.getUserRole().getAuthorities());
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
