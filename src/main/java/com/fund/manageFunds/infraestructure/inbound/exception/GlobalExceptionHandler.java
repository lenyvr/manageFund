package com.fund.manageFunds.infraestructure.inbound.exception;

import com.fund.manageFunds.domain.exceptions.IsAlreadySubscribed;
import com.fund.manageFunds.domain.exceptions.NoAvailableAmount;
import com.fund.manageFunds.domain.exceptions.NoClientFound;
import com.fund.manageFunds.domain.exceptions.NoFundFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IsAlreadySubscribed.class)
    public ResponseEntity<ErrorResponse> manageExistingSubscription(IsAlreadySubscribed ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse errorBody = getErrorBody(HttpStatus.CONFLICT, ex.getMessage());
        return new ResponseEntity<>(errorBody, status);
    }

    @ExceptionHandler(NoAvailableAmount.class)
    public ResponseEntity<ErrorResponse> manageAmountError(NoAvailableAmount ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse errorBody = getErrorBody(status, ex.getMessage());
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(errorBody, status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> manageGenericError(RuntimeException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorBody = getErrorBody(status, "An unexpected error occurred.");
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(errorBody, status);
    }

    @ExceptionHandler(NoFundFound.class)
    public ResponseEntity<ErrorResponse> manageFundExistenceError(NoFundFound ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse errorBody = getErrorBody(status, ex.getMessage());
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(errorBody, status);
    }

    @ExceptionHandler(NoClientFound.class)
    public ResponseEntity<ErrorResponse> manageFundExistenceError(NoClientFound ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse errorBody = getErrorBody(status, ex.getMessage());
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(errorBody, status);
    }

    private ErrorResponse getErrorBody(HttpStatus status, String message){
        return new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message
        );
    }
}
