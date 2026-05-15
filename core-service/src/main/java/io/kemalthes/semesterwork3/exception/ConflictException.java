package io.kemalthes.semesterwork3.exception;

public class ConflictException extends ServiceException {

    public ConflictException(String message) {
        super(message, 409);
    }
}
