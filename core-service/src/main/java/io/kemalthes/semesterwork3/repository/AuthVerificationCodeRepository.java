package io.kemalthes.semesterwork3.repository;

import io.kemalthes.semesterwork3.entity.AuthVerificationCode;
import io.kemalthes.semesterwork3.entity.enums.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AuthVerificationCodeRepository extends JpaRepository<AuthVerificationCode, UUID> {

    @Query("""
        from AuthVerificationCode
        where purpose = :purpose and consumedAt is null and email = :email
        order by createdAt desc
        limit 1
    """)
    Optional<AuthVerificationCode> findFirstCodeByParams(String email, VerificationPurpose purpose);
}
