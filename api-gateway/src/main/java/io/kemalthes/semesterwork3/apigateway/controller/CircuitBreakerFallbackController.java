package io.kemalthes.semesterwork3.apigateway.controller;

import io.kemalthes.semesterwork3.apigateway.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class CircuitBreakerFallbackController {

    @RequestMapping("/search-error")
    public Mono<ResponseEntity<ErrorResponse>> searchErrorFallback() {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .message("Search service is temporarily unavailable. Please try again later.")
                .path("/fallback/search-error")
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }
}
