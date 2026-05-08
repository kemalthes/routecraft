package io.kemalthes.semesterwork3.exception;

import org.springframework.http.HttpStatus;

public class InternalMinioException extends ServiceException {
    public InternalMinioException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
