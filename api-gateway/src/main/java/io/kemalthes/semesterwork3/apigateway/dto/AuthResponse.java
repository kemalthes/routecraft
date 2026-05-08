package io.kemalthes.semesterwork3.apigateway.dto;

import java.util.UUID;

public record AuthResponse(UUID userId, String role) {
}
