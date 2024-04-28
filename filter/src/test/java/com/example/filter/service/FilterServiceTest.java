package com.example.filter.service;

import com.example.filter.api.imagefilter.ConcreteImageFilter;
import com.example.filter.api.repository.ProcessedRepository;
import com.example.filter.config.BaseTest;
import com.example.filter.dto.kafka.image.ImageDone;
import com.example.filter.dto.kafka.image.ImageFilterRequest;
import com.example.filter.model.entity.Processed;
import com.example.filter.model.enumeration.FilterType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.times;

@Transactional
class FilterServiceTest extends BaseTest {

    @Autowired
    private FilterService filterService;
    @MockBean
    private KafkaTemplate<String, Object> imageTemplate;
    @MockBean
    private MinioService minioService;
    @Autowired
    private ProcessedRepository processedRepository;
    @MockBean
    private ConcreteImageFilter filter;

    @Test
    void consume_LastFilter() {
        var imageId = "imageId";
        var requestId = "requestId";
        var filters = new ArrayList<>(List.of(FilterType.GRAY));
        var imageFilterRequest = new ImageFilterRequest(imageId, requestId, filters);

        var originalImage = "original".getBytes();
        Mockito.when(minioService.download(imageId)).thenReturn(originalImage);

        var modifiedImage = "modified".getBytes();
        Mockito.when(filter.convert(originalImage)).thenReturn(modifiedImage);
        Mockito.when(filter.getFilterType()).thenReturn(FilterType.GRAY);

        var completeFuture = new CompletableFuture<SendResult<String, Object>>();
        completeFuture.complete(Mockito.mock(SendResult.class));
        Mockito.when(imageTemplate.send(Mockito.any(), Mockito.any())).thenReturn(completeFuture);

        var acknowledge = Mockito.mock(Acknowledgment.class);
        filterService.consume(imageFilterRequest, acknowledge);

        Mockito.verify(minioService, times(1)).download(imageId);
        Mockito.verify(minioService, times(1)).uploadFile(Mockito.eq(modifiedImage),
                Mockito.anyString());

        Assertions.assertTrue(processedRepository.existsByOriginalAndRequest(imageId, requestId));

        Mockito.verify(imageTemplate, times(1)).send(Mockito.anyString(), Mockito.any());
        Mockito.verify(acknowledge, Mockito.times(1)).acknowledge();
    }

    @Test
    void consume_NotLastFilter() {
        var imageId = "imageId";
        var requestId = "requestId";
        var filters = new ArrayList<>(List.of(FilterType.GRAY, FilterType.SAMPLE));
        var imageFilterRequest = new ImageFilterRequest(imageId, requestId, filters);

        var originalImage = "original".getBytes();
        Mockito.when(minioService.download(imageId)).thenReturn(originalImage);

        var modifiedImage = "modified".getBytes();
        Mockito.when(filter.convert(originalImage)).thenReturn(modifiedImage);
        Mockito.when(filter.getFilterType()).thenReturn(FilterType.GRAY);

        var completeFuture = new CompletableFuture<SendResult<String, Object>>();
        completeFuture.complete(Mockito.mock(SendResult.class));
        Mockito.when(imageTemplate.send(Mockito.any(), Mockito.any())).thenReturn(completeFuture);

        var acknowledge = Mockito.mock(Acknowledgment.class);
        filterService.consume(imageFilterRequest, acknowledge);

        Mockito.verify(minioService, times(1)).download(imageId);
        Mockito.verify(minioService, times(1)).uploadTmpFile(Mockito.eq(modifiedImage),
                Mockito.anyString());

        Assertions.assertTrue(processedRepository.existsByOriginalAndRequest(imageId, requestId));

        Mockito.verify(imageTemplate, times(1)).send(Mockito.anyString(), Mockito.any());
        Mockito.verify(acknowledge, Mockito.times(1)).acknowledge();
    }

    @Test
    void consume_CanNotProcess() {
        var imageId = "imageId";
        var requestId = "requestId";
        var filters = new ArrayList<>(List.of(FilterType.SAMPLE));
        var imageFilterRequest = new ImageFilterRequest(imageId, requestId, filters);

        var acknowledge = Mockito.mock(Acknowledgment.class);
        filterService.consume(imageFilterRequest, acknowledge);

        Assertions.assertFalse(processedRepository.existsByOriginalAndRequest(imageId, requestId));

        Mockito.verify(acknowledge, Mockito.never()).acknowledge();
    }

    @Test
    void consume_AlreadyProcessed() {
        var imageId = "imageId";
        var requestId = "requestId";
        var filters = new ArrayList<>(List.of(FilterType.GRAY));
        var imageFilterRequest = new ImageFilterRequest(imageId, requestId, filters);

        processedRepository.save(new Processed(null, imageId, requestId));

        var acknowledge = Mockito.mock(Acknowledgment.class);
        filterService.consume(imageFilterRequest, acknowledge);

        Mockito.verify(acknowledge, Mockito.never()).acknowledge();
    }

}
