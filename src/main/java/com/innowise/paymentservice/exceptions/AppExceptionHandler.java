package com.innowise.paymentservice.exceptions;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@Log4j2
@ControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<String> handleGeneralException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(exception = {NoPaymentRelatedToSuchOrderException.class,
            NoSuchElementException.class})
    public ResponseEntity<String> handleNotFoundExceptions(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({
            WrongHeaderException.class,
            PaymentAlreadyProcessedException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<String> handleBadRequestExceptions(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(PaymentProcessException.class)
    public ResponseEntity<String> externalServiceException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<String> authorizationDeniedExceptionException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
}
