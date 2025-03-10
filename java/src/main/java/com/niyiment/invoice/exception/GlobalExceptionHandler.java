package com.niyiment.invoice.exception;


import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * Global exception handler for the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles InvoiceNotFoundException and returns a 404 response.
     *
     * @param ex The exception
     * @param request The web request
     * @return 404 response with error details
     */
    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleInvoiceNotFoundException(InvoiceNotFoundException ex, WebRequest request) {
        return createErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handles BadRequestException and returns a 400 response.
     *
     * @param ex The exception
     * @param request The web request
     * @return 400 response with error details
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex, WebRequest request) {
        return createErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles DuplicateKeyException from MongoDB and returns a 409 response.
     *
     * @param ex The exception
     * @param request The web request
     * @return 409 response with error details
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorDetails> handleDuplicateKeyException(DuplicateKeyException ex, WebRequest request) {
        return createErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    /**
     * Handles validation errors and returns a 400 response with field error details.
     *
     * @param ex The exception
     * @param headers The HTTP headers
     * @param status The HTTP status
     * @param request The web request
     * @return 400 response with validation error details
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorDetails errorDetails = new ValidationErrorDetails(
                LocalDateTime.now(),
                "Validation Error",
                errors,
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other exceptions and returns a 500 response.
     *
     * @param ex The exception
     * @param request The web request
     * @return 500 response with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        return createErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Creates an error response with the given exception, status, and request.
     *
     * @param ex The exception
     * @param status The HTTP status
     * @param request The web request
     * @return Response entity with error details
     */
    private ResponseEntity<ErrorDetails> createErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, status);
    }

    /**
     * DTO for error details.
     */
    public static class ErrorDetails {
        private LocalDateTime timestamp;
        private String message;
        private String details;

        public ErrorDetails(LocalDateTime timestamp, String message, String details) {
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public String getDetails() {
            return details;
        }
    }

    /**
     * DTO for validation error details.
     */
    public static class ValidationErrorDetails extends ErrorDetails {
        private Map<String, String> errors;

        public ValidationErrorDetails(LocalDateTime timestamp, String message, Map<String, String> errors, String details) {
            super(timestamp, message, details);
            this.errors = errors;
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }
}

