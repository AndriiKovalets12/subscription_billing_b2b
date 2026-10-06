package dev.users;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public SecurityUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NotBlank String username) throws UsernameNotFoundException {
        return userRepository.
                findByEmail(username).
                map(SecurityUser::new).
                orElseThrow(() -> new EntityNotFoundException("User with email " + username + " not found."));
    }
}
