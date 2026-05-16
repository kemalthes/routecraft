package io.kemalthes.searchservice.controller.handler;

import io.kemalthes.search.dto.SearchErrorResponse;
import io.kemalthes.searchservice.exception.SearchServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<SearchErrorResponse> handleBadRequest(Exception e, HttpServletRequest request) {
        log.warn("Invalid search request: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "BadRequest", e.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<SearchErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Search access denied: {}", e.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", e.getMessage(), request);
    }

    @ExceptionHandler(SearchServiceException.class)
    public ResponseEntity<SearchErrorResponse> handleSearchService(SearchServiceException e, HttpServletRequest request) {
        log.error("Search service error: {}", e.getMessage(), e);
        return buildResponse(e.getStatus(), e.getStatus().getReasonPhrase(), e.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SearchErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("Unexpected search service error", e);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "InternalServerError",
                "Unexpected search service error",
                request
        );
    }

    private ResponseEntity<SearchErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status)
                .body(new SearchErrorResponse()
                        .status(status.value())
                        .error(error)
                        .message(message)
                        .path(request.getRequestURI()));
    }
}
