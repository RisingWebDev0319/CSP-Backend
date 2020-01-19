package ca.freshstart.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.UNAUTHORIZED)  // 401
public class UnathorizedException extends RuntimeException {
    public UnathorizedException(String msg) {
        super(msg);
    }
}