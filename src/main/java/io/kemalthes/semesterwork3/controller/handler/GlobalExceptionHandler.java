package io.kemalthes.semesterwork3.controller.handler;

import io.kemalthes.core.dto.ErrorResponse;
import io.kemalthes.semesterwork3.exception.ServiceException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errors);
        return buildResponse(
                "ValidationError",
                "Validation failed for: %s".formatted(errors),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationExceptions(Exception e) {
        log.warn("Bad request (validation): {}", e.getMessage());
        return buildResponse("ValidationError", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception e) {
        log.warn("Malformed request payload: {}", e.getMessage());
        return buildResponse(
                "BadRequest",
                "Malformed JSON request or invalid parameters format.",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException e) {
        log.warn("Business logic exception: {} (Status: {})", e.getMessage(), e.getStatus());
        return buildResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.valueOf(e.getStatus()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException e) {
        log.error("Database error (DataAccessException): ", e);
        return buildResponse("DatabaseError",
                "A data integrity error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        String incidentId = UUID.randomUUID().toString();
        log.error("Unexpected server error. IncidentID: {}", incidentId, e);
        return buildResponse(
                "InternalServerError",
                "An unexpected error occurred. Please contact support and mention Incident ID: " + incidentId,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String name, String message, HttpStatus status) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(status.value());
        response.setError(name);
        response.setMessage(message);
        return ResponseEntity.status(status).body(response);
    }
}
