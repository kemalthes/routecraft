package io.kemalthes.semesterwork3.exception;

public class UserNotFoundException extends ServiceException {

    public UserNotFoundException(Object routeId) {
        super("User with id=%s was not found".formatted(routeId), 404);
    }
}

