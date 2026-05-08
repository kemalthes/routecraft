package io.kemalthes.semesterwork3.exception;

public class BadRequestException extends ServiceException {

    public BadRequestException(String message) {
        super(message, 400);
    }
}
