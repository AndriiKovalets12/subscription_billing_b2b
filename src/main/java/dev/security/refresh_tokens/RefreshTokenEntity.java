package dev.security.refresh_tokens;

import dev.users.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.time.OffsetDateTime;

@Entity
@Getter
@Builder
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh-tokens_gen_seq")
    @SequenceGenerator(name = "refresh-tokens_gen_seq", sequenceName = "refresh-tokens_seq")
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;

    @Column(name = "expiring_time", nullable = false)
    private OffsetDateTime expiringTime;

    public RefreshTokenEntity() {
    }

    public RefreshTokenEntity(String token,
                              UserEntity user,
                              @Value("${application.security.refresh-token.refresh-token-expiration-day}") Long expiringTime) {
        this.token = token;
        this.user = user;
        this.expiringTime = OffsetDateTime.now().plusDays(expiringTime);
    }

}
