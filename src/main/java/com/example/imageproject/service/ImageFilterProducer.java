package com.example.imageproject.service;

import com.example.imageproject.dto.kafka.image.ImageFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageFilterProducer {

    @Value("${spring.kafka.topic.outbound-topic}")
    private String out;

    private final KafkaTemplate<String, ImageFilter> kafkaTemplate;

    public void send(ImageFilter value) {
        kafkaTemplate.send(out, value);
    }

}
