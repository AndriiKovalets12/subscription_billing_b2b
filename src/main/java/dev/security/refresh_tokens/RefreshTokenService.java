package dev.security.refresh_tokens;

import dev.users.UserEntity;
import dev.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {
    @Value("${application.security.refresh-token.refresh-token-expiration-day}")
    private long expirationDays;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshTokenEntity createRefreshToken(Long userId){
        refreshTokenRepository.deleteById(userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RefreshTokenEntity refreshToken =
                new RefreshTokenEntity(UUID.randomUUID().toString(),
                        user,
                        expirationDays);


        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity token) {
        if (token.getExpiringTime().isBefore(OffsetDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

}
