package com.example.api.service;

import com.example.api.dto.kafka.image.ImageDone;
import com.example.api.dto.kafka.image.ImageFilter;
import com.example.api.dto.mapper.FilterMapper;
import com.example.api.dto.rest.image.ApplyImageFiltersResponse;
import com.example.api.dto.rest.image.GetModifiedImageByRequestIdResponse;
import com.example.api.exception.EntityNotFoundException;
import com.example.api.exception.IllegalAccessException;
import com.example.api.model.entity.FilterRequest;
import com.example.api.model.enumeration.FilterType;
import com.example.api.model.enumeration.ImageStatus;
import com.example.api.repository.FilterRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilterRequestService {

    @Value("${spring.kafka.topic.processing-topic}")
    private String processing;

    private static final Integer MAX_ATTEMPTS = 3;
    private static final Long BACKOFF_PERIOD = 500L;

    private final FilterRequestRepository filterRequestRepository;
    private final ImageService imageService;
    private final KafkaTemplate<String, ImageFilter> kafkaTemplate;
    private final FilterMapper imageFilterMapper;

    @Transactional
    public ApplyImageFiltersResponse createRequest(String imageId, List<FilterType> filters) {
        if (!imageService.validateAccess(imageId)) {
            throw new IllegalAccessException("You are not the owner of this image");
        }

        var image = imageService.getImageByImageId(imageId);
        var requestId = UUID.randomUUID().toString();

        var filterRequest = imageFilterMapper.toFilterRequest(image, requestId);
        filterRequest = filterRequestRepository.save(filterRequest);

        var imageFilter = imageFilterMapper.toImageFilter(filterRequest, filters);
        sendRequest(imageFilter);

        return new ApplyImageFiltersResponse(requestId);
    }

    private void sendRequest(ImageFilter request) {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            try {
                try {
                    kafkaTemplate.send(processing, request).get();

                    return;
                } catch (ExecutionException e) {
                    attempts++;

                    Thread.sleep(BACKOFF_PERIOD);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        throw new KafkaException("Failed to send request to Kafka after maximum attempts");
    }

    public GetModifiedImageByRequestIdResponse getRequest(String requestId, String imageId) {
        if (!imageService.validateAccess(imageId)) {
            throw new IllegalAccessException("You are not the owner of this image");
        }

        var filterRequest = getFilterRequestByRequestId(requestId);

        if (!filterRequest.getOriginalId().equals(imageId)) {
            throw new EntityNotFoundException("There is no request for this image with such an id");
        }

        return new GetModifiedImageByRequestIdResponse(filterRequest.getStatus() == ImageStatus.WIP ?
                filterRequest.getOriginalId() : filterRequest.getModifiedId(), filterRequest.getStatus());
    }

    public FilterRequest getFilterRequestByRequestId(String requestId) {
        return filterRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new EntityNotFoundException("There is no request with such an id"));
    }

    @Transactional
    @KafkaListener(topics = "${spring.kafka.topic.done-topic}",
            groupId = "images.done-consumer-group-1",
            containerFactory = "doneFactory")
    public void consume(ImageDone imageDone, Acknowledgment acknowledgment) {
        var filterRequest = getFilterRequestByRequestId(imageDone.getRequestId());
        var modifiedImageId = imageDone.getImageId();

        filterRequest.setModifiedId(modifiedImageId);
        filterRequest.setStatus(ImageStatus.DONE);
        filterRequestRepository.save(filterRequest);

        imageService.saveImage(filterRequest.getOriginalId(), modifiedImageId);

        acknowledgment.acknowledge();
    }

}
