package vn.edu.iuh.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
