package vn.edu.iuh.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MessageRecallTimeExpiredException extends RuntimeException {
    public MessageRecallTimeExpiredException(String message) {
        super(message);
    }
}
