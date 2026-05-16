package io.kemalthes.searchservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SearchServiceException extends RuntimeException {

    private final HttpStatus status;

    public SearchServiceException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

}
