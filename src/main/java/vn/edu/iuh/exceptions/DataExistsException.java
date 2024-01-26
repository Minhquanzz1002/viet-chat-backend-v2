package vn.edu.iuh.exceptions;

public class DataExistsException extends RuntimeException {
    public DataExistsException(String message) {
        super(message);
    }
}
