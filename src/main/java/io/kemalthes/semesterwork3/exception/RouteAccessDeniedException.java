package io.kemalthes.semesterwork3.exception;

public class RouteAccessDeniedException extends ServiceException {

    public RouteAccessDeniedException() {
        super("You do not have permission to modify this route", 403);
    }
}
