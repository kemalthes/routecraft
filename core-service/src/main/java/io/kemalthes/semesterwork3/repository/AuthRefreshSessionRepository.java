package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.AuthRefreshSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthRefreshSessionRepository extends JpaRepository<AuthRefreshSession, UUID> {

    Optional<AuthRefreshSession> findByRefreshTokenHash(String refreshTokenHash);

    List<AuthRefreshSession> findAllByUserIdAndRevokedAtIsNull(UUID userId);
}
