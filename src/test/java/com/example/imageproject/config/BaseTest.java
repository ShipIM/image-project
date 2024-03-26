package com.example.imageproject.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@DirtiesContext
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = BaseTest.Initializer.class)
@TestPropertySource(properties = {"spring.config.location=classpath:application.yml"})
public abstract class BaseTest {

    private static final String DATABASE_NAME = "image-project";

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:latest")
                    .withReuse(true)
                    .withDatabaseName(DATABASE_NAME);

    @Container
    public static MinIOContainer minIOContainer = new MinIOContainer("minio/minio:latest")
            .withReuse(true)
            .withUserName("user")
            .withPassword("password");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "minio.url=" + minIOContainer.getS3URL()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}
