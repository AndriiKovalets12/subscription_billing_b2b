package dev.invitation.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {
    Optional<InvitationEntity> findByToken(String token);
}
