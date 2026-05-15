package io.kemalthes.semesterwork3.entity;

import io.kemalthes.semesterwork3.entity.enums.VerificationPurpose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "auth_verification_codes")
public class AuthVerificationCode {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "code_hash", length = 128, nullable = false)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 32, nullable = false)
    private VerificationPurpose purpose;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;
}
