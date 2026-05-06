package io.kemalthes.semesterwork3.exception;

public class ReviewNotFoundException extends ServiceException {

    public ReviewNotFoundException(Object reviewId) {
        super("Review with id=%s was not found".formatted(reviewId), 404);
    }
}
