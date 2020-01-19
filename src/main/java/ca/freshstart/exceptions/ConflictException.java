package ca.freshstart.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.CONFLICT)  // 409
public class ConflictException extends RuntimeException {
    public ConflictException(String msg) {
        super(msg);
    }
}