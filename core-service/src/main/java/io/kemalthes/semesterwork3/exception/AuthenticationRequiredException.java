package io.kemalthes.semesterwork3.exception;

public class AuthenticationRequiredException extends ServiceException {

    public AuthenticationRequiredException() {
        super("Authentication is required", 401);
    }
}
