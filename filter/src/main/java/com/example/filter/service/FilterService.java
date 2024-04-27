package com.example.filter.service;

import com.example.filter.api.imagefilter.ConcreteImageFilter;
import com.example.filter.api.repository.ProcessedRepository;
import com.example.filter.dto.kafka.image.ImageDone;
import com.example.filter.dto.kafka.image.ImageFilterRequest;
import com.example.filter.model.entity.Processed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ExecutionException;


@Service
@Slf4j
@RequiredArgsConstructor
public class FilterService {

    @Value("${spring.kafka.topic.processing-topic}")
    private String processing;
    @Value("${spring.kafka.topic.done-topic}")
    private String done;

    private final KafkaTemplate<String, Object> imageTemplate;

    private final MinioService minioService;
    private final ProcessedRepository processedRepository;

    private final ConcreteImageFilter filter;

    @Transactional
    @KafkaListener(
            topics = "${spring.kafka.topic.processing-topic}",
            containerFactory = "processingFactory",
            concurrency = "${spring.kafka.topic.partitions-number}"
    )
    public void consume(ImageFilterRequest imageFilterRequest, Acknowledgment acknowledgment) {
        if (imageFilterRequest.getFilters().get(0) != filter.getFilterType() ||
                processedRepository.existsByOriginalAndRequest(imageFilterRequest.getImageId(),
                        imageFilterRequest.getRequestId())) {
            return;
        }

        imageFilterRequest.getFilters().remove(0);

        var original = minioService.download(imageFilterRequest.getImageId());

        var modified = filter.convert(original);
        var modifiedId = UUID.randomUUID().toString();

        processedRepository.save(new Processed(null, imageFilterRequest.getImageId(),
                imageFilterRequest.getRequestId()));

        try {
            if (imageFilterRequest.getFilters().isEmpty()) {
                minioService.uploadFile(modified, modifiedId);

                var response = new ImageDone(modifiedId, imageFilterRequest.getRequestId());
                imageTemplate.send(done, response).get();
            } else {
                minioService.uploadTmpFile(modified, modifiedId);

                var response = new ImageFilterRequest(modifiedId, imageFilterRequest.getRequestId(),
                        imageFilterRequest.getFilters());
                imageTemplate.send(processing, response).get();
            }

            acknowledgment.acknowledge();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Unable to process image, an error occurred: {}", e.getMessage(), e);

            minioService.delete(modifiedId);
        }
    }

}
