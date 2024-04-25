package com.example.filtergray.service;

import com.example.filtergray.dto.kafka.image.ImageDone;
import com.example.filtergray.dto.kafka.image.ImageFilter;
import com.example.filtergray.model.entity.Processed;
import com.example.filtergray.model.enumeration.FilterType;
import com.example.filtergray.repository.ProcessedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilterService {

    private static final FilterType TYPE = FilterType.GRAY;

    @Value("${spring.kafka.topic.processing-topic}")
    private String processing;
    @Value("${spring.kafka.topic.done-topic}")
    private String done;

    private final KafkaTemplate<String, ImageDone> doneTemplate;
    private final KafkaTemplate<String, ImageFilter> filterTemplate;

    private final MinioService minioService;
    private final ProcessedRepository processedRepository;

    @Transactional
    @KafkaListener(
            topics = "${spring.kafka.topic.processing-topic}",
            containerFactory = "processingFactory",
            concurrency = "${spring.kafka.topic.partitions-number}"
    )
    public void consume(ImageFilter imageFilter, Acknowledgment acknowledgment) {
        if (imageFilter.getFilters().get(0) != TYPE ||
                processedRepository.existsByOriginalAndRequest(imageFilter.getImageId(), imageFilter.getRequestId())) {
            return;
        }
        imageFilter.getFilters().remove(0);

        var modifiedId = process(imageFilter);

        if (imageFilter.getFilters().isEmpty()) {
            var response = new ImageDone(modifiedId, imageFilter.getRequestId());
            doneTemplate.send(done, response)
                    .whenComplete((result, exception) -> {
                        if (Objects.isNull(exception)) {
                            acknowledgment.acknowledge();
                        }
                    });
        } else {
            var response = new ImageFilter(modifiedId, imageFilter.getRequestId(),
                    imageFilter.getFilters());
            filterTemplate.send(processing, response)
                    .whenComplete((result, exception) -> {
                        if (Objects.isNull(exception)) {
                            acknowledgment.acknowledge();
                        }
                    });
        }
    }

    private String process(ImageFilter imageFilter) {
        var original = minioService.download(imageFilter.getImageId());

        var modifiedId = UUID.randomUUID().toString();

        processedRepository.save(new Processed(null, imageFilter.getImageId(), imageFilter.getRequestId(),
                modifiedId));
        if (imageFilter.getFilters().isEmpty()) {
            minioService.uploadFile(original, modifiedId);
        } else {
            minioService.uploadTmpFile(original, modifiedId);
        }

        return modifiedId;
    }

}
