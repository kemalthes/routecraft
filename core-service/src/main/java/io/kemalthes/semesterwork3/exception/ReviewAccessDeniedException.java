package io.kemalthes.semesterwork3.exception;

public class ReviewAccessDeniedException extends ServiceException {

    public ReviewAccessDeniedException() {
        super("You do not have permission to modify this review", 403);
    }
}
