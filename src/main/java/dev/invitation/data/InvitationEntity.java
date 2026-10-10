package dev.invitation.data;

import dev.tenants.data.TenantEntity;
import dev.users.security.UserRole;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "invitations")
@Getter
public class InvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_invitations_seq")
    @SequenceGenerator(sequenceName = "invitations_seq", name = "gen_invitations_seq")
    private Long id;

    @Column(name = "token", unique = true, nullable = false)
    private String token;

    @Column(name = "email", unique = true, nullable = false, length = 75)
    private String email;

    @Column(name = "user_role", nullable = false, length = 14)
    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    public InvitationEntity() {
    }

    public InvitationEntity(String token, String email, UserRole userRole, TenantEntity tenant, OffsetDateTime expiresAt) {
        this.token = token;
        this.email = email;
        this.userRole = userRole;
        this.tenant = tenant;
        this.expiresAt = expiresAt;
    }

}
