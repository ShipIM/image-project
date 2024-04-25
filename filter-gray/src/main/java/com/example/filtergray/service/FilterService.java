package com.example.filtergray.service;

import com.example.filtergray.api.repository.ProcessedRepository;
import com.example.filtergray.dto.kafka.image.ImageDone;
import com.example.filtergray.dto.kafka.image.ImageFilterRequest;
import com.example.filtergray.imagefilter.GrayFilter;
import com.example.filtergray.model.entity.Processed;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FilterService {

    @Value("${spring.kafka.topic.processing-topic}")
    private String processing;
    @Value("${spring.kafka.topic.done-topic}")
    private String done;

    private final KafkaTemplate<String, ImageDone> doneTemplate;
    private final KafkaTemplate<String, ImageFilterRequest> filterTemplate;

    private final MinioService minioService;
    private final ProcessedRepository processedRepository;

    private final GrayFilter grayFilter;

    @Transactional
    @KafkaListener(
            topics = "${spring.kafka.topic.processing-topic}",
            containerFactory = "processingFactory",
            concurrency = "${spring.kafka.topic.partitions-number}"
    )
    public void consume(ImageFilterRequest imageFilterRequest, Acknowledgment acknowledgment) throws IOException {
        if (imageFilterRequest.getFilters().get(0) != grayFilter.getFilterType() ||
                processedRepository.existsByOriginalAndRequest(imageFilterRequest.getImageId(),
                        imageFilterRequest.getRequestId())) {
            return;
        }
        imageFilterRequest.getFilters().remove(0);

        var modifiedId = process(imageFilterRequest);

        if (imageFilterRequest.getFilters().isEmpty()) {
            var response = new ImageDone(modifiedId, imageFilterRequest.getRequestId());
            doneTemplate.send(done, response)
                    .whenComplete((result, exception) -> {
                        if (Objects.isNull(exception)) {
                            acknowledgment.acknowledge();
                        }
                    });
        } else {
            var response = new ImageFilterRequest(modifiedId, imageFilterRequest.getRequestId(),
                    imageFilterRequest.getFilters());
            filterTemplate.send(processing, response)
                    .whenComplete((result, exception) -> {
                        if (Objects.isNull(exception)) {
                            acknowledgment.acknowledge();
                        }
                    });
        }
    }

    private String process(ImageFilterRequest imageFilterRequest) throws IOException {
        var original = minioService.download(imageFilterRequest.getImageId());

        var modified = grayFilter.convert(original);
        var modifiedId = UUID.randomUUID().toString();

        processedRepository.save(new Processed(null, imageFilterRequest.getImageId(),
                imageFilterRequest.getRequestId(),
                modifiedId));
        if (imageFilterRequest.getFilters().isEmpty()) {
            minioService.uploadFile(modified, modifiedId);
        } else {
            minioService.uploadTmpFile(modified, modifiedId);
        }

        return modifiedId;
    }

}
