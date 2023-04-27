package com.hegetomi.taskify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException manve) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, manve.getMessage());
        detail.setType(URI.create("taskify/invalid-command"));
        return detail;
    }
    @ExceptionHandler(UserExistsException.class)
    public ProblemDetail handleUserExistsException(UserExistsException uee) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, uee.getMessage());
        detail.setType(URI.create("taskify/user-exists"));
        return detail;
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException unne) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, unne.getMessage());
        detail.setType(URI.create("taskify/user-not-found"));
        return detail;
    }
    @ExceptionHandler(InvalidOldPasswordException.class)
    public ProblemDetail handleInvalidOldPasswordException(InvalidOldPasswordException iope) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, iope.getMessage());
        detail.setType(URI.create("taskify/invalid-old-password"));
        return detail;
    }
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleUAuthenticationException(AuthenticationException ae) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ae.getMessage());
        detail.setType(URI.create("taskify/authentication-error"));
        return detail;
    }
    @ExceptionHandler(TicketNotFoundException.class)
    public ProblemDetail handleTicketNotFoundException(TicketNotFoundException tnfe) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, tnfe.getMessage());
        detail.setType(URI.create("taskify/ticket-not-found"));
        return detail;
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatchException(MethodArgumentTypeMismatchException tnfe) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, tnfe.getMessage());
        detail.setType(URI.create("taskify/type-mismatch"));
        return detail;
    }
}