package com.example.filtergray.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;

@SpringBootTest
@Testcontainers
@DirtiesContext
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = BaseTest.Initializer.class)
public abstract class BaseTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:latest")
                    .withReuse(true)
                    .withDatabaseName("filter-gray");

    @Container
    public static MinIOContainer minIOContainer = new MinIOContainer("minio/minio:latest")
            .withReuse(true)
            .withUserName("user")
            .withPassword("password");

    @Container
    public static final KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                    .withReuse(true)
                    .withEmbeddedZookeeper()
                    .withCopyFileToContainer(MountableFile.forHostPath(Paths.get("../configs").toAbsolutePath()), "/etc/kafka/configs")
                    .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "BROKER:SASL_PLAINTEXT,PLAINTEXT:SASL_PLAINTEXT")
                    .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "BROKER")
                    .withEnv("KAFKA_BROKER_ID", "1")
                    .withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
                    .withEnv("ZOOKEEPER_SASL_ENABLED", "false")
                    .withEnv("KAFKA_OPTS", "-Djava.security.auth.login.config=/etc/kafka/configs/kafka_server_jaas.conf")
                    .withEnv("KAFKA_SASL_ENABLED_MECHANISMS", "PLAIN")
                    .withEnv("KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL", "PLAIN");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "minio.url=" + minIOContainer.getS3URL(),
                    "spring.kafka.bootstrap-servers=" + kafkaContainer.getBootstrapServers(),
                    "spring.kafka.topic.replication-factor=1"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}

