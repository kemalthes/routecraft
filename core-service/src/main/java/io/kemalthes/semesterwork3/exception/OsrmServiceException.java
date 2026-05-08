package io.kemalthes.semesterwork3.exception;

public class OsrmServiceException extends ServiceException {

    public OsrmServiceException(String message) {
        super(message, 502);
    }
}
