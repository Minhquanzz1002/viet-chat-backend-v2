package vn.edu.iuh.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.edu.iuh.dto.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO
                .builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .detail(Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage())
                .build();
        return ResponseEntity.badRequest().body(errorResponseDTO);
    }

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

    @ExceptionHandler({FriendshipRelationshipException.class, FileUploadException.class, UserNotInChatException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleFriendshipRelationshipException(RuntimeException exception) {
        return ErrorResponseDTO
                .builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
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

    @ExceptionHandler({OTPMismatchException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleOTPMismatchException(RuntimeException exception) {
        return exception.getMessage();
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
