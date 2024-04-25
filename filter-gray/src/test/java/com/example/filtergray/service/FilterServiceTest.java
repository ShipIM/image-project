package com.example.filtergray.service;

import com.example.filtergray.api.imagefilter.ConcreteImageFilter;
import com.example.filtergray.api.repository.ProcessedRepository;
import com.example.filtergray.config.BaseTest;
import com.example.filtergray.dto.kafka.image.ImageDone;
import com.example.filtergray.dto.kafka.image.ImageFilterRequest;
import com.example.filtergray.model.entity.Processed;
import com.example.filtergray.model.enumeration.FilterType;
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
    private KafkaTemplate<String, ImageDone> doneTemplate;
    @MockBean
    private KafkaTemplate<String, ImageFilterRequest> filterTemplate;
    @MockBean
    private MinioService minioService;
    @Autowired
    private ProcessedRepository processedRepository;
    @MockBean
    private ConcreteImageFilter filter;

    @Test
    void consume_LastFilter() throws IOException {
        var imageId = "imageId";
        var requestId = "requestId";
        var filters = new ArrayList<>(List.of(FilterType.GRAY));
        var imageFilterRequest = new ImageFilterRequest(imageId, requestId, filters);

        var originalImage = "original".getBytes();
        Mockito.when(minioService.download(imageId)).thenReturn(originalImage);

        var modifiedImage = "modified".getBytes();
        Mockito.when(filter.convert(originalImage)).thenReturn(modifiedImage);
        Mockito.when(filter.getFilterType()).thenReturn(FilterType.GRAY);

        var completeFuture = new CompletableFuture<SendResult<String, ImageDone>>();
        completeFuture.complete(Mockito.mock(SendResult.class));
        Mockito.when(doneTemplate.send(Mockito.any(), Mockito.any())).thenReturn(completeFuture);

        var acknowledge = Mockito.mock(Acknowledgment.class);
        filterService.consume(imageFilterRequest, acknowledge);

        Mockito.verify(minioService, times(1)).download(imageId);
        Mockito.verify(minioService, times(1)).uploadFile(Mockito.eq(modifiedImage),
                Mockito.anyString());

        Assertions.assertTrue(processedRepository.existsByOriginalAndRequest(imageId, requestId));

        Mockito.verify(doneTemplate, times(1)).send(Mockito.anyString(), Mockito.any());
        Mockito.verify(acknowledge, Mockito.times(1)).acknowledge();
    }

    @Test
    void consume_NotLastFilter() throws IOException {
        var imageId = "imageId";
        var requestId = "requestId";
        var filters = new ArrayList<>(List.of(FilterType.GRAY, FilterType.SAMPLE));
        var imageFilterRequest = new ImageFilterRequest(imageId, requestId, filters);

        var originalImage = "original".getBytes();
        Mockito.when(minioService.download(imageId)).thenReturn(originalImage);

        var modifiedImage = "modified".getBytes();
        Mockito.when(filter.convert(originalImage)).thenReturn(modifiedImage);
        Mockito.when(filter.getFilterType()).thenReturn(FilterType.GRAY);

        var completeFuture = new CompletableFuture<SendResult<String, ImageFilterRequest>>();
        completeFuture.complete(Mockito.mock(SendResult.class));
        Mockito.when(filterTemplate.send(Mockito.any(), Mockito.any())).thenReturn(completeFuture);

        var acknowledge = Mockito.mock(Acknowledgment.class);
        filterService.consume(imageFilterRequest, acknowledge);

        Mockito.verify(minioService, times(1)).download(imageId);
        Mockito.verify(minioService, times(1)).uploadTmpFile(Mockito.eq(modifiedImage),
                Mockito.anyString());

        Assertions.assertTrue(processedRepository.existsByOriginalAndRequest(imageId, requestId));

        Mockito.verify(filterTemplate, times(1)).send(Mockito.anyString(), Mockito.any());
        Mockito.verify(acknowledge, Mockito.times(1)).acknowledge();
    }

    @Test
    void consume_CanNotProcess() throws IOException {
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
    void consume_AlreadyProcessed() throws IOException {
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
