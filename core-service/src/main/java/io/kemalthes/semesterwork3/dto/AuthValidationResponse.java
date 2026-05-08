package io.kemalthes.semesterwork3.dto;

import java.util.UUID;

public record AuthValidationResponse(UUID userId, String role) {
}
