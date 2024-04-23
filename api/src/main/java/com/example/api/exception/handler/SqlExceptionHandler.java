package com.example.api.exception.handler;

import com.example.api.dto.rest.error.UiSuccessContainer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class SqlExceptionHandler {
    private final Map<String, String> violationsMap;

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public UiSuccessContainer handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        var container = new UiSuccessContainer();
        container.setSuccess(false);

        var message = violationsMap.entrySet().stream()
                .filter(entry -> exception.getMessage().contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findAny()
                .orElse("An unexpected exception has occurred");
        container.setMessage(message);

        return container;
    }

}
