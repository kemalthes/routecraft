package io.kemalthes.semesterwork3.service;

import io.kemalthes.core.dto.LoginRequest;
import io.kemalthes.semesterwork3.config.props.AuthProperties;
import io.kemalthes.semesterwork3.dto.JwtClaims;
import io.kemalthes.semesterwork3.entity.AuthRefreshSession;
import io.kemalthes.semesterwork3.entity.Role;
import io.kemalthes.semesterwork3.entity.User;
import io.kemalthes.semesterwork3.entity.enums.RoleName;
import io.kemalthes.semesterwork3.exception.AuthenticationRequiredException;
import io.kemalthes.semesterwork3.repository.AuthRefreshSessionRepository;
import io.kemalthes.semesterwork3.repository.AuthVerificationCodeRepository;
import io.kemalthes.semesterwork3.repository.RoleRepository;
import io.kemalthes.semesterwork3.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AuthRefreshSessionRepository refreshSessionRepository;
    private AuthVerificationCodeRepository verificationCodeRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenService jwtTokenService;
    private AuthMailService authMailService;
    private AuthProperties authProperties;
    private StringRedisTemplate stringRedisTemplate;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        refreshSessionRepository = mock(AuthRefreshSessionRepository.class);
        verificationCodeRepository = mock(AuthVerificationCodeRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtTokenService = mock(JwtTokenService.class);
        authMailService = mock(AuthMailService.class);
        authProperties = new AuthProperties();
        authProperties.getJwt().setRefreshTokenTtl(Duration.ofDays(30));
        stringRedisTemplate = mock(StringRedisTemplate.class);
        authService = new AuthService(
                userRepository,
                roleRepository,
                refreshSessionRepository,
                verificationCodeRepository,
                passwordEncoder,
                jwtTokenService,
                authMailService,
                authProperties,
                stringRedisTemplate
        );
    }

    @Test
    void checkEmailExistsNormalizesEmailBeforeLookup() {
        when(authMailService.normalizeEmail(" User@Example.COM ")).thenReturn("user@example.com");
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        var response = authService.checkEmailExists(" User@Example.COM ");

        assertEquals(true, response.getExists());
    }

    @Test
    void loginCreatesRefreshSessionAndReturnsTokens() {
        UUID userId = UUID.randomUUID();
        User user = user(userId, "user", RoleName.USER);

        when(authMailService.normalizeEmail("User@Example.com")).thenReturn("user@example.com");
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hash")).thenReturn(true);
        when(jwtTokenService.createRefreshToken(eq(userId), any(UUID.class), any(OffsetDateTime.class)))
                .thenReturn("refresh-token");
        when(jwtTokenService.sha256("refresh-token")).thenReturn("refresh-hash");
        when(refreshSessionRepository.save(any(AuthRefreshSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.createAccessToken(eq(userId), any(UUID.class), eq("ROLE_USER")))
                .thenReturn("access-token");

        var response = authService.login(new LoginRequest("User@Example.com", "password"), "127.0.0.1", "  agent  ");

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("ROLE_USER", response.getRole().getValue());
        verify(refreshSessionRepository).save(argThatSession(user, "refresh-hash", "127.0.0.1", "agent"));
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = user(UUID.randomUUID(), "user", RoleName.USER);
        when(authMailService.normalizeEmail("user@example.com")).thenReturn("user@example.com");
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThrows(AuthenticationRequiredException.class,
                () -> authService.login(new LoginRequest("user@example.com", "bad"), "127.0.0.1", null));

        verify(refreshSessionRepository, never()).save(any());
    }

    @Test
    void validateTokenReturnsClaimsWhenSessionIsActive() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        JwtClaims claims = new JwtClaims(
                userId,
                sessionId,
                "ROLE_USER",
                "jwt-id",
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(15).toEpochSecond()
        );
        AuthRefreshSession session = activeSession(sessionId, user(userId, "user", RoleName.USER));

        when(jwtTokenService.parseAccessToken("access-token")).thenReturn(claims);
        when(stringRedisTemplate.hasKey("auth:blacklist:access:jwt-id")).thenReturn(false);
        when(refreshSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        var response = authService.validateToken("Bearer access-token", "127.0.0.1");

        assertEquals(userId, response.getUserId());
        assertEquals("ROLE_USER", response.getRole().getValue());
    }

    @Test
    void validateTokenRejectsBlacklistedToken() {
        JwtClaims claims = new JwtClaims(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ROLE_USER",
                "jwt-id",
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(15).toEpochSecond()
        );

        when(jwtTokenService.parseAccessToken("access-token")).thenReturn(claims);
        when(stringRedisTemplate.hasKey("auth:blacklist:access:jwt-id")).thenReturn(true);

        assertThrows(AuthenticationRequiredException.class,
                () -> authService.validateToken("Bearer access-token", "127.0.0.1"));

        verify(refreshSessionRepository, never()).findById(any());
    }

    private static AuthRefreshSession argThatSession(User user, String tokenHash, String clientIp, String userAgent) {
        return org.mockito.ArgumentMatchers.argThat(session -> {
            assertNotNull(session.getId());
            assertEquals(user, session.getUser());
            assertEquals(tokenHash, session.getRefreshTokenHash());
            assertEquals(clientIp, session.getClientIp());
            assertEquals(userAgent, session.getUserAgent());
            assertNotNull(session.getCreatedAt());
            assertNotNull(session.getExpiresAt());
            return true;
        });
    }

    private static AuthRefreshSession activeSession(UUID sessionId, User user) {
        AuthRefreshSession session = new AuthRefreshSession();
        session.setId(sessionId);
        session.setUser(user);
        session.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));
        return session;
    }

    private static User user(UUID id, String username, RoleName roleName) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        Role role = new Role();
        role.setName(roleName);
        user.getRoles().add(role);
        return user;
    }
}
