package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.AuthCodeRequest;
import io.kemalthes.core.dto.AuthResponse;
import io.kemalthes.core.dto.AuthValidationResponse;
import io.kemalthes.core.dto.ChangePasswordRequest;
import io.kemalthes.core.dto.CheckEmailExistsResponse;
import io.kemalthes.core.dto.LoginRequest;
import io.kemalthes.core.dto.RefreshTokenRequest;
import io.kemalthes.core.dto.RegisterRequest;
import io.kemalthes.core.dto.ResetPasswordRequest;
import io.kemalthes.semesterwork3.config.props.AuthProperties;
import io.kemalthes.semesterwork3.dto.JwtClaims;
import io.kemalthes.semesterwork3.entity.AuthRefreshSession;
import io.kemalthes.semesterwork3.entity.AuthVerificationCode;
import io.kemalthes.semesterwork3.entity.Role;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.entity.enums.RoleName;
import io.kemalthes.semesterwork3.entity.enums.VerificationPurpose;
import io.kemalthes.semesterwork3.exception.AuthenticationRequiredException;
import io.kemalthes.semesterwork3.exception.BadRequestException;
import io.kemalthes.semesterwork3.exception.ConflictException;
import io.kemalthes.semesterwork3.repository.AuthRefreshSessionRepository;
import io.kemalthes.semesterwork3.repository.AuthVerificationCodeRepository;
import io.kemalthes.semesterwork3.repository.RoleRepository;
import io.kemalthes.semesterwork3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BLACKLIST_PREFIX = "auth:blacklist:access:";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthRefreshSessionRepository refreshSessionRepository;
    private final AuthVerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthMailService authMailService;
    private final AuthProperties authProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public CheckEmailExistsResponse checkEmailExists(String email) {
        boolean exists = userRepository.existsByEmailIgnoreCase(authMailService.normalizeEmail(email));
        return new CheckEmailExistsResponse(exists);
    }

    @Transactional
    public void sendRegistrationCode(AuthCodeRequest request) {
        String email = authMailService.normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered");
        }
        authMailService.saveAndSendCode(email, VerificationPurpose.REGISTRATION, "RouteCraft registration code");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String clientIp, String userAgent) {
        String email = authMailService.normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername().trim())) {
            throw new ConflictException("Username is already registered");
        }
        authMailService.consumeCode(email, request.getCode(), VerificationPurpose.REGISTRATION);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(request.getUsername().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("USER role is not seeded"));
        user.getRoles().add(userRole);
        userRepository.save(user);
        return createSessionResponse(user, clientIp, userAgent);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp, String userAgent) {
        User user = userRepository.findByEmailIgnoreCase(authMailService.normalizeEmail(request.getEmail()))
                .orElseThrow(AuthenticationRequiredException::new);
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationRequiredException();
        }
        return createSessionResponse(user, clientIp, userAgent);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, String clientIp, String userAgent) {
        JwtClaims refreshClaims = jwtTokenService.parseRefreshToken(request.getRefreshToken());
        String refreshTokenHash = jwtTokenService.sha256(request.getRefreshToken());
        AuthRefreshSession session = refreshSessionRepository.findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(AuthenticationRequiredException::new);
        assertSessionActive(session, clientIp);
        if (!session.getId().equals(refreshClaims.sessionId())
                || !session.getUser().getId().equals(refreshClaims.userId())) {
            throw new AuthenticationRequiredException();
        }
        String nextRefreshToken = jwtTokenService.createRefreshToken(
                session.getUser().getId(),
                session.getId(),
                session.getExpiresAt()
        );
        session.setRefreshTokenHash(jwtTokenService.sha256(nextRefreshToken));
        session.setRotatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        session.setUserAgent(trimUserAgent(userAgent));
        refreshSessionRepository.save(session);
        String role = resolveRole(session.getUser());
        String accessToken = jwtTokenService.createAccessToken(session.getUser().getId(), session.getId(), role);
        return new AuthResponse()
                .accessToken(accessToken)
                .refreshToken(nextRefreshToken)
                .role(AuthResponse.RoleEnum.fromValue(role));
    }

    @Transactional
    public void logout(String authHeader, String clientIp) {
        JwtClaims claims = parseBearer(authHeader);
        blacklistAccessToken(claims);
        AuthRefreshSession session = refreshSessionRepository.findById(claims.sessionId())
                .orElseThrow(AuthenticationRequiredException::new);
        assertSessionActive(session, clientIp);
        session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String authHeader, String clientIp) {
        JwtClaims claims = parseBearer(authHeader);
        AuthRefreshSession session = refreshSessionRepository.findById(claims.sessionId())
                .orElseThrow(AuthenticationRequiredException::new);
        assertSessionActive(session, clientIp);
        User user = session.getUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthenticationRequiredException();
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        revokeUserSessions(user.getId());
        blacklistAccessToken(claims);
    }

    @Transactional
    public void sendPasswordResetCode(AuthCodeRequest request) {
        String email = authMailService.normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            authMailService.saveAndSendCode(email, VerificationPurpose.PASSWORD_RESET, "RouteCraft password reset code");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = authMailService.normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(AuthenticationRequiredException::new);
        authMailService.consumeCode(email, request.getCode(), VerificationPurpose.PASSWORD_RESET);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        revokeUserSessions(user.getId());
    }

    @Transactional(readOnly = true)
    public AuthValidationResponse validateToken(String authHeader, String clientIp) {
        JwtClaims claims = parseBearer(authHeader);
        String blacklistKey = BLACKLIST_PREFIX + claims.jwtId();
        if (stringRedisTemplate.hasKey(blacklistKey)) {
            throw new AuthenticationRequiredException();
        }
        AuthRefreshSession session = refreshSessionRepository.findById(claims.sessionId())
                .orElseThrow(AuthenticationRequiredException::new);
        assertSessionActive(session, clientIp);
        if (!session.getUser().getId().equals(claims.userId())) {
            throw new AuthenticationRequiredException();
        }
        return new AuthValidationResponse()
                .userId(claims.userId())
                .role(AuthValidationResponse.RoleEnum.fromValue(claims.role()));
    }

    private AuthResponse createSessionResponse(User user, String clientIp, String userAgent) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        AuthRefreshSession session = new AuthRefreshSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setClientIp(clientIp);
        session.setUserAgent(trimUserAgent(userAgent));
        session.setCreatedAt(now);
        session.setExpiresAt(now.plus(authProperties.getJwt().getRefreshTokenTtl()));
        String refreshToken = jwtTokenService.createRefreshToken(
                user.getId(),
                session.getId(),
                session.getExpiresAt()
        );
        session.setRefreshTokenHash(jwtTokenService.sha256(refreshToken));
        refreshSessionRepository.save(session);
        String role = resolveRole(user);
        String accessToken = jwtTokenService.createAccessToken(user.getId(), session.getId(), role);
        return new AuthResponse()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(AuthResponse.RoleEnum.fromValue(role));
    }

    private JwtClaims parseBearer(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationRequiredException();
        }
        return jwtTokenService.parseAccessToken(authHeader.substring("Bearer ".length()).trim());
    }

    private void assertSessionActive(AuthRefreshSession session, String clientIp) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (session.getRevokedAt() != null || !session.getExpiresAt().isAfter(now)) {
            throw new AuthenticationRequiredException();
        }
    }

    private void blacklistAccessToken(JwtClaims claims) {
        stringRedisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + claims.jwtId(),
                claims.userId().toString(),
                jwtTokenService.ttlSeconds(claims),
                TimeUnit.SECONDS
        );
    }

    private void revokeUserSessions(UUID userId) {
        OffsetDateTime revokedAt = OffsetDateTime.now(ZoneOffset.UTC);
        refreshSessionRepository.findAllByUserIdAndRevokedAtIsNull(userId)
                .forEach(session -> session.setRevokedAt(revokedAt));
    }

    private String resolveRole(User user) {
        boolean isAdmin = user.getRoles() != null && user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName -> roleName == RoleName.ADMIN);
        return isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
    }

    private String trimUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        String trimmed = userAgent.trim();
        return trimmed.length() > 512 ? trimmed.substring(0, 512) : trimmed;
    }
}
