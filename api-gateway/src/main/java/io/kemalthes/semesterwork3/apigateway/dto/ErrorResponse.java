package io.kemalthes.semesterwork3.apigateway.dto;

import lombok.Builder;

@Builder
public record ErrorResponse(Integer status, String error, String message, String path) {
}
