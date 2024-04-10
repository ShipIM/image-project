package com.example.imageproject.config.kafka;

import com.example.imageproject.dto.kafka.image.ImageFilter;
import com.example.imageproject.dto.kafka.image.ImageDone;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaProperties properties;

    @Value("${spring.kafka.topic.outbound-topic}")
    private String outbound;
    @Value("${spring.kafka.topic.inbound-topic}")
    private String inbound;
    @Value("${spring.kafka.sasl.username}")
    private String username;
    @Value("${spring.kafka.sasl.password}")
    private String password;

    @Bean
    public NewTopic topicWip() {
        return new NewTopic(outbound, 1, (short) 3);
    }

    @Bean
    public NewTopic topicDone() {
        return new NewTopic(inbound, 1, (short) 3);
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(adminProps());
    }

    @Bean
    public KafkaTemplate<String, ImageFilter> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ImageDone> kafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ImageDone>();
        factory.setConsumerFactory(consumerFactory());

        return factory;
    }

    private ProducerFactory<String, ImageFilter> producerFactory() {
       return new DefaultKafkaProducerFactory<>(producerProps());
    }

    private ConsumerFactory<String, ImageDone> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerProps());
    }

    private Map<String, Object> adminProps() {
        var props = properties.buildAdminProperties(null);

        props.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
                "%s required username=\"%s\" password=\"%s\";", PlainLoginModule.class.getName(), username, password
        ));

        return props;
    }

    private Map<String, Object> producerProps() {
        var props = properties.buildProducerProperties(null);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(ProducerConfig.ACKS_CONFIG, "all");

        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
                "%s required username=\"%s\" password=\"%s\";", PlainLoginModule.class.getName(), username, password
        ));

        return props;
    }

    private Map<String, Object> consumerProps() {
        var props = properties.buildConsumerProperties(null);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
                "%s required username=\"%s\" password=\"%s\";", PlainLoginModule.class.getName(), username, password
        ));

        return props;
    }

}
