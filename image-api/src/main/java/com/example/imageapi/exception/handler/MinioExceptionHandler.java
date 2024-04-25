package com.example.imageapi.exception.handler;

import com.example.imageapi.dto.rest.error.UiSuccessContainer;
import com.example.imageapi.exception.UncheckedMinioException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class MinioExceptionHandler {

    @ExceptionHandler(value = UncheckedMinioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public UiSuccessContainer handleErrorResponseException(UncheckedMinioException exception) {
        return new UiSuccessContainer(false, exception.getMessage());
    }

}
