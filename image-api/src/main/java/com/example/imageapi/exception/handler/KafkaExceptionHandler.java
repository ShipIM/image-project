package com.example.imageapi.exception.handler;

import com.example.imageapi.dto.rest.error.UiSuccessContainer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.KafkaException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class KafkaExceptionHandler {

    @ExceptionHandler(value = KafkaException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public UiSuccessContainer handleErrorResponseException(KafkaException exception) {
        return new UiSuccessContainer(false, exception.getMessage());
    }

}
