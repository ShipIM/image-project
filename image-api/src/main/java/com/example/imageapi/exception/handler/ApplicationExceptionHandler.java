package com.example.imageapi.exception.handler;

import com.example.imageapi.dto.rest.error.UiSuccessContainer;
import com.example.imageapi.exception.EntityNotFoundException;
import com.example.imageapi.exception.IllegalAccessException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(value = EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public UiSuccessContainer handleNotFoundException(EntityNotFoundException exception) {
        return new UiSuccessContainer(false, exception.getMessage());
    }

    @ExceptionHandler(value = IllegalAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public UiSuccessContainer handleIllegalAccessException(IllegalAccessException exception) {
        return new UiSuccessContainer(false, exception.getMessage());
    }

}
