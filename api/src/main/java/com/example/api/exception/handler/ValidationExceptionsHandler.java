package com.example.api.exception.handler;

import com.example.api.dto.rest.error.UiSuccessContainer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidationExceptionsHandler {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public UiSuccessContainer handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        var container = new UiSuccessContainer();
        container.setSuccess(false);

        var builder = new StringBuilder();
        exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .forEach(message -> builder.append(message).append("\n"));
        container.setMessage(builder.toString());

        return container;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public UiSuccessContainer handleConstraintViolationException(ConstraintViolationException ex) {
        var container = new UiSuccessContainer();
        container.setSuccess(false);

        var builder = new StringBuilder();
        ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .forEach(message -> builder.append(message).append("\n"));
        container.setMessage(builder.toString());

        return container;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public UiSuccessContainer handleValidationException(ValidationException exception) {
        return new UiSuccessContainer(false, exception.getMessage());
    }

}
