package com.example.imageproject.service;

import com.example.imageproject.dto.kafka.image.ImageFilter;
import com.example.imageproject.dto.mapper.FilterMapper;
import com.example.imageproject.dto.rest.image.ApplyImageFiltersResponse;
import com.example.imageproject.dto.rest.image.GetModifiedImageByRequestIdResponse;
import com.example.imageproject.exception.EntityNotFoundException;
import com.example.imageproject.exception.IllegalAccessException;
import com.example.imageproject.model.enumeration.FilterType;
import com.example.imageproject.repository.FilterRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
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

    private static final Integer MAX_ATTEMPTS = 3;
    private static final Long BACKOFF_PERIOD = 500L;

    private final FilterRequestRepository filterRequestRepository;
    private final ImageService imageService;
    private final KafkaTemplate<String, ImageFilter> kafkaTemplate;
    private final FilterMapper imageFilterMapper;

    @Transactional
    public ApplyImageFiltersResponse createRequest(String imageId, List<FilterType> filters) {
        System.out.println("retry");
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
                    kafkaTemplate.send(out, request).get();

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
        var filterRequest = filterRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new EntityNotFoundException("There is no request with such an id"));

        if (!imageService.validateAccess(imageId)) {
            throw new IllegalAccessException("You are not the owner of this image");
        }

        return imageFilterMapper.toResponse(filterRequest);
    }

}
