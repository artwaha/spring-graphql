package com.atwaha.sis.components;

import com.atwaha.sis.model.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final HttpServletRequest request;
    private final UtilityMethods util;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<?> response = util.generateErrorResponse(HttpStatus.BAD_REQUEST, request.getRequestURI(), "Invalid Method Arguments", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        Class<?> requiredType = exception.getRequiredType();
        Object value = exception.getValue();

        Map<String, String> errors = Map.of(
                "Required Type", requiredType == null ? "null" : requiredType.getSimpleName(),
                "Provided Value", value == null ? "null" : value.toString()
        );

        String message = "Invalid argument type for " + exception.getName();
        String path = request.getRequestURI();

        ApiResponse<?> response = util.generateErrorResponse(HttpStatus.BAD_REQUEST, path, message, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFoundException(EntityNotFoundException exception) {
        ApiResponse<?> response = util.generateErrorResponse(HttpStatus.NOT_FOUND, request.getRequestURI(), exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
