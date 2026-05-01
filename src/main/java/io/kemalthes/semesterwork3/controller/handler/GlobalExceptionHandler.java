package io.kemalthes.semesterwork3.controller.handler;

import io.kemalthes.core.dto.ErrorResponse;
import io.kemalthes.core.dto.FieldErrorDto;
import io.kemalthes.core.dto.ValidationErrorResponse;
import io.kemalthes.semesterwork3.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDto> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new FieldErrorDto()
                        .field(err.getField())
                        .message(err.getDefaultMessage()))
                .toList();
        return buildValidationResponse("Validation data error", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());
        List<FieldErrorDto> errors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return new FieldErrorDto()
                            .field(field)
                            .message(violation.getMessage());
                })
                .toList();
        return buildValidationResponse("Ошибка валидации параметров", request, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodValidationException(
            HandlerMethodValidationException ex, HttpServletRequest request) {
        log.warn("Handler method validation failed: {}", ex.getMessage());
        List<FieldErrorDto> errors = new ArrayList<>();
        ex.getAllValidationResults().forEach(result -> {
            String field = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(err ->
                    errors.add(new FieldErrorDto()
                            .field(field)
                            .message(err.getDefaultMessage()))
            );
        });
        return buildValidationResponse("Handler method validation failed", request, errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch for parameter {}: {}", ex.getName(), ex.getMessage());
        String message = String.format("Неверный формат данных. Ожидался тип: %s",
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "неизвестно");
        List<FieldErrorDto> errors = List.of(new FieldErrorDto()
                .field(ex.getName())
                .message(message));
        return buildValidationResponse("Type mismatch error", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed request payload: {}", ex.getMessage());
        return buildValidationResponse("Malformed JSON request or invalid parameters format",
                request, Collections.emptyList());
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


    private ResponseEntity<ValidationErrorResponse> buildValidationResponse(
            String message, HttpServletRequest request, List<FieldErrorDto> errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(message)
                        .path(request.getRequestURI())
                        .validationErrors(errors));
    }
}
