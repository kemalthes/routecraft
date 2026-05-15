package io.kemalthes.semesterwork3.service;

import io.kemalthes.semesterwork3.config.props.AuthProperties;
import io.kemalthes.semesterwork3.entity.AuthVerificationCode;
import io.kemalthes.semesterwork3.entity.enums.VerificationPurpose;
import io.kemalthes.semesterwork3.exception.BadRequestException;
import io.kemalthes.semesterwork3.repository.AuthVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthMailService {

    private final AuthVerificationCodeRepository verificationCodeRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final AuthProperties authProperties;
    private final JwtTokenService jwtTokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    public void sendCode(String email, String code, String subject) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("Auth mail is disabled. Verification code for {}: {}", email, code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(authProperties.getMail().getFrom());
        message.setTo(email);
        message.setSubject(subject);
        message.setText("Your RouteCraft verification code: " + code);
        mailSender.send(message);
    }

    public String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public void consumeCode(String email, String code, VerificationPurpose purpose) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Verification code is required");
        }
        AuthVerificationCode verificationCode = verificationCodeRepository
                .findFirstCodeByParams(email, purpose)
                .orElseThrow(() -> new BadRequestException("Verification code is invalid"));
        if (verificationCode.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new BadRequestException("Verification code has expired");
        }
        if (!verificationCode.getCodeHash().equals(jwtTokenService.sha256(code.trim()))) {
            throw new BadRequestException("Verification code is invalid");
        }
        verificationCode.setConsumedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    public void saveAndSendCode(String email, VerificationPurpose purpose, String subject) {
        String code = "%06d".formatted(secureRandom.nextInt(1_000_000));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        AuthVerificationCode verificationCode = new AuthVerificationCode();
        verificationCode.setId(UUID.randomUUID());
        verificationCode.setEmail(email);
        verificationCode.setPurpose(purpose);
        verificationCode.setCodeHash(jwtTokenService.sha256(code));
        verificationCode.setCreatedAt(now);
        verificationCode.setExpiresAt(now.plus(authProperties.getVerification().getCodeTtl()));
        verificationCodeRepository.save(verificationCode);
        sendCode(email, code, subject);
    }
}
