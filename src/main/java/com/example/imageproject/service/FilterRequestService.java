package com.example.imageproject.service;

import com.example.imageproject.dto.kafka.image.ImageDone;
import com.example.imageproject.dto.kafka.image.ImageFilter;
import com.example.imageproject.dto.mapper.FilterMapper;
import com.example.imageproject.dto.rest.image.ApplyImageFiltersResponse;
import com.example.imageproject.dto.rest.image.GetModifiedImageByRequestIdResponse;
import com.example.imageproject.exception.EntityNotFoundException;
import com.example.imageproject.exception.IllegalAccessException;
import com.example.imageproject.model.entity.FilterRequest;
import com.example.imageproject.model.enumeration.FilterType;
import com.example.imageproject.model.enumeration.ImageStatus;
import com.example.imageproject.repository.FilterRequestRepository;
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

    @Value("${spring.kafka.topic.outbound-topic}")
    private String out;
    @Value("${spring.kafka.backoff.interval}")
    private Long interval;
    @Value("${spring.kafka.backoff.max-failure}")
    private Long failures;

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
        while (attempts < failures) {
            try {
                try {
                    kafkaTemplate.send(out, request).get();

                    return;
                } catch (ExecutionException e) {
                    attempts++;

                    Thread.sleep(interval);
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
    @KafkaListener(topics = "${spring.kafka.topic.inbound-topic}",
            groupId = "images.done-consumer-group-1",
            containerFactory = "imageDoneKafkaListenerContainerFactory")
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
