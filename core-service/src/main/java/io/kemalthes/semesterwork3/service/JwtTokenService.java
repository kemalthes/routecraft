package io.kemalthes.semesterwork3.service;

import io.jsonwebtoken.io.Encoders;
import io.kemalthes.semesterwork3.config.props.AuthProperties;
import io.kemalthes.semesterwork3.dto.JwtClaims;
import io.kemalthes.semesterwork3.exception.AuthenticationRequiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String SESSION_ID = "sid";
    private static final String ROLE = "role";

    private final AuthProperties authProperties;

    public String createAccessToken(UUID userId, UUID sessionId, String role) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = now.plus(authProperties.getJwt().getAccessTokenTtl());
        return Jwts.builder()
                .subject(userId.toString())
                .claim(SESSION_ID, sessionId.toString())
                .claim(ROLE, role)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(expiresAt.toInstant()))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(UUID userId, UUID sessionId, OffsetDateTime expiresAt) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return Jwts.builder()
                .subject(userId.toString())
                .claim(SESSION_ID, sessionId.toString())
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(expiresAt.toInstant()))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    public JwtClaims parseAccessToken(String token) {
        return parseAccessToken(token, ACCESS_TOKEN_TYPE);
    }

    public JwtClaims parseRefreshToken(String token) {
        return parseAccessToken(token, REFRESH_TOKEN_TYPE);
    }

    public String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Encoders.BASE64URL.encode(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    public long ttlSeconds(JwtClaims claims) {
        long ttl = claims.expiresAt() - OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond();
        return Math.max(ttl, 1);
    }

    private JwtClaims parseAccessToken(String token, String expectedTokenType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!expectedTokenType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                throw new AuthenticationRequiredException();
            }
            Instant expiresAt = claims.getExpiration().toInstant();
            return new JwtClaims(
                    UUID.fromString(claims.getSubject()),
                    UUID.fromString(claims.get(SESSION_ID, String.class)),
                    claims.get(ROLE, String.class),
                    claims.getId(),
                    expiresAt.getEpochSecond()
            );
        } catch (JwtException | IllegalArgumentException | NullPointerException exception) {
            throw new AuthenticationRequiredException();
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
