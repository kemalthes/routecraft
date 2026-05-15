package io.kemalthes.semesterwork3.exception;

public class UserAccessDeniedException extends ServiceException {

    public UserAccessDeniedException() {
        super("You have not access to this resource", 403);
    }
}
