package com.example.imageapi.service;

import com.example.imageapi.api.repository.FilterRequestRepository;
import com.example.imageapi.dto.kafka.image.ImageDone;
import com.example.imageapi.dto.kafka.image.ImageFilterRequest;
import com.example.imageapi.dto.mapper.FilterMapper;
import com.example.imageapi.dto.rest.image.ApplyImageFiltersResponse;
import com.example.imageapi.dto.rest.image.GetModifiedImageByRequestIdResponse;
import com.example.imageapi.exception.EntityNotFoundException;
import com.example.imageapi.exception.IllegalAccessException;
import com.example.imageapi.exception.TooManyRequestsException;
import com.example.imageapi.model.entity.FilterRequest;
import com.example.imageapi.model.entity.User;
import com.example.imageapi.model.enumeration.FilterType;
import com.example.imageapi.model.enumeration.ImageStatus;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilterRequestService {

    @Value("${spring.kafka.topic.processing-topic}")
    private String processing;
    @Value("${spring.kafka.backoff.interval}")
    private Long interval;
    @Value("${spring.kafka.backoff.max-failure}")
    private Long failures;

    private final FilterRequestRepository filterRequestRepository;
    private final ImageService imageService;
    private final KafkaTemplate<String, ImageFilterRequest> kafkaTemplate;
    private final FilterMapper imageFilterMapper;
    private final BucketService bucketService;

    @Transactional
    public ApplyImageFiltersResponse createRequest(String imageId, List<FilterType> filters) {
        if (!imageService.validateAccess(imageId)) {
            throw new IllegalAccessException("You are not the owner of this image");
        }

        if (filters.contains(FilterType.RECOGNITION)) {
             var auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
             var bucket = bucketService.getBucketByUsername(auth.getUsername());

             if (!bucket.tryConsume(1)) {
                 throw new TooManyRequestsException("Too many requests per period");
             }
        }

        var image = imageService.getImageByImageId(imageId);
        var requestId = UUID.randomUUID().toString();

        var filterRequest = imageFilterMapper.toFilterRequest(image, requestId);
        filterRequest = filterRequestRepository.save(filterRequest);

        var imageFilter = imageFilterMapper.toImageFilter(filterRequest, filters);
        sendRequest(imageFilter);

        return new ApplyImageFiltersResponse(requestId);
    }

    private void sendRequest(ImageFilterRequest request) {
        int attempts = 0;
        while (attempts < failures) {
            try {
                try {
                    kafkaTemplate.send(processing, request).get();

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

        return new GetModifiedImageByRequestIdResponse(filterRequest.getStatus() == ImageStatus.DONE ?
                filterRequest.getModifiedId() : filterRequest.getOriginalId(),
                filterRequest.getStatus(),
                filterRequest.getMessage());
    }

    public FilterRequest getFilterRequestByRequestId(String requestId) {
        return filterRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new EntityNotFoundException("There is no request with such an id"));
    }

    @Transactional
    @KafkaListener(
            topics = "${spring.kafka.topic.done-topic}",
            groupId = "images.done-consumer-group-1",
            containerFactory = "doneFactory",
            concurrency = "${spring.kafka.topic.partitions-number}"
    )
    public void consume(ImageDone imageDone, Acknowledgment acknowledgment) {
        var filterRequest = getFilterRequestByRequestId(imageDone.getRequestId());
        if (filterRequest.getStatus().equals(ImageStatus.DONE) || filterRequest.getStatus().equals(ImageStatus.FAIL)) {
            return;
        }

        var status = imageDone.getStatus();
        if (status.equals(ImageStatus.DONE)) {
            var modifiedImageId = imageDone.getImageId();

            filterRequest.setModifiedId(modifiedImageId);
            filterRequest.setStatus(ImageStatus.DONE);

            imageService.saveImage(filterRequest.getOriginalId(), modifiedImageId);
        } else {
            filterRequest.setStatus(ImageStatus.FAIL);
            filterRequest.setMessage(imageDone.getMessage());
        }

        filterRequestRepository.save(filterRequest);

        acknowledgment.acknowledge();
    }

}
