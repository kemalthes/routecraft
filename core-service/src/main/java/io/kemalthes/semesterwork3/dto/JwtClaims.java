package io.kemalthes.semesterwork3.dto;

import java.util.UUID;

public record JwtClaims(UUID userId, UUID sessionId, String role, String jwtId, long expiresAt) {
    }