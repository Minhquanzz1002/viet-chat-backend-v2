package vn.edu.iuh.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.edu.iuh.dto.ErrorResponseDTO;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {UnauthorizedException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDTO handleBadCredentialsException(RuntimeException exception) {
        return ErrorResponseDTO
                .builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .detail(exception.getMessage())
                .build();
    }

    @ExceptionHandler({DataNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleDataNotFoundException(RuntimeException exception) {
        return ErrorResponseDTO
                .builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .detail(exception.getMessage())
                .build();
    }

    @ExceptionHandler({DataExistsException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleDataExistsException(RuntimeException exception) {
        return ErrorResponseDTO
                .builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .detail(exception.getMessage())
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleException(RuntimeException exception) {
        return ErrorResponseDTO
                .builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .detail(exception.getMessage())
                .build();
    }


}
