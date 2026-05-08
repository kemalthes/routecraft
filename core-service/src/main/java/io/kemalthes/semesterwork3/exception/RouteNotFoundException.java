package io.kemalthes.semesterwork3.exception;

public class RouteNotFoundException extends ServiceException {

    public RouteNotFoundException(Object routeId) {
        super("Route with id=%s was not found".formatted(routeId), 404);
    }
}
