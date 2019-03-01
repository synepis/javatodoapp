package io.github.synepis.todo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(
                ApiError.builder()
                        .status(HttpStatus.FORBIDDEN.name())
                        .userMessages(List.of("Access is denied"))
                        .debugMessages(List.of(ex.getMessage()))
                        .createdOn(Instant.now())
                        .build(),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var bindingResult = ex.getBindingResult();

        var errorMessages = bindingResult.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.toList());

        return new ResponseEntity<>(
                ApiError.builder()
                        .status(HttpStatus.BAD_REQUEST.name())
                        .userMessages(errorMessages)
                        .debugMessages(errorMessages)
                        .createdOn(clock.instant())
                        .build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    private ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>(
                ApiError.builder()
                        .status(HttpStatus.UNAUTHORIZED.name())
                        .userMessages(List.of("Bad credentials were provided"))
                        .debugMessages(List.of(ex.getMessage()))
                        .createdOn(clock.instant())
                        .build(),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<ApiError> defaultExceptionHandler(Exception ex) {
        return new ResponseEntity<>(
                ApiError.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .userMessages(List.of("Unknown error"))
                        .debugMessages(List.of(ex.getMessage()))
                        .createdOn(clock.instant())
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
