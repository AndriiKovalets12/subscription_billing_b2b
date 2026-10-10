package dev.security.refresh_tokens;

import dev.users.data.UserEntity;
import dev.users.data.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {
    private final PasswordEncoder passwordEncoder;
    @Value("${application.security.refresh-token.refresh-token-expiration-day}")
    private long expirationDays;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RefreshTokenEntity createRefreshToken(Long userId){
        refreshTokenRepository.deleteByUserId(userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RefreshTokenEntity refreshToken =
                new RefreshTokenEntity(
                        passwordEncoder.encode(UUID.randomUUID().toString()),
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
