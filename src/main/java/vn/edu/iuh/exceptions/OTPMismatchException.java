package vn.edu.iuh.exceptions;

public class OTPMismatchException extends RuntimeException {
    public OTPMismatchException(String message) {
        super(message);
    }
}
