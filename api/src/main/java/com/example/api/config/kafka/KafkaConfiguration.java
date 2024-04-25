package com.example.api.config.kafka;

import com.example.api.dto.kafka.image.ImageDone;
import com.example.api.dto.kafka.image.ImageFilter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaProperties properties;

    @Value("${spring.kafka.topic.processing-topic}")
    private String processing;
    @Value("${spring.kafka.topic.partitions-number}")
    private Integer numPartitions;
    @Value("${spring.kafka.topic.replication-factor}")
    private Short replicationFactor;
    @Value("${spring.kafka.sasl.username}")
    private String username;
    @Value("${spring.kafka.sasl.password}")
    private String password;

    @Bean
    public NewTopic topicWip() {
        return new NewTopic(processing, numPartitions, replicationFactor);
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(adminProps());
    }

    @Bean
    public KafkaTemplate<String, ImageFilter> kafkaTemplate() {
        return new KafkaTemplate<>(imageFilterProducerFactory());
    }

    @Bean("doneFactory")
    public ConcurrentKafkaListenerContainerFactory<String, ImageDone> doneFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ImageDone>();

        factory.setConsumerFactory(imageDoneConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    private ProducerFactory<String, ImageFilter> imageFilterProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerProps());
    }

    private ConsumerFactory<String, ImageDone> imageDoneConsumerFactory() {
        var props = consumerProps();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.api.dto.kafka.image.ImageDone");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    private Map<String, Object> adminProps() {
        var props = properties.buildAdminProperties(null);

        props.putAll(saslProps());

        return props;
    }

    private Map<String, Object> producerProps() {
        var props = properties.buildProducerProperties(null);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(ProducerConfig.ACKS_CONFIG, "all");

        props.put(ProducerConfig.RETRIES_CONFIG, 1);

        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        props.putAll(saslProps());

        return props;
    }

    private Map<String, Object> consumerProps() {
        var props = properties.buildConsumerProperties(null);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        props.putAll(saslProps());

        return props;
    }

    private Map<String, Object> saslProps() {
        return Map.of(
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT",
                SaslConfigs.SASL_MECHANISM, "PLAIN",
                SaslConfigs.SASL_JAAS_CONFIG, String.format("%s required username=\"%s\" password=\"%s\";",
                        PlainLoginModule.class.getName(), username, password)
        );
    }

}
