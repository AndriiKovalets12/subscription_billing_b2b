package dev.security.refresh_tokens;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    void deleteByUserId(Long userId);
}
