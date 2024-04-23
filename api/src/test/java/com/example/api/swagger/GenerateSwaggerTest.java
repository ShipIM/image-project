package com.example.api.swagger;

import com.example.api.config.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class GenerateSwaggerTest extends BaseTest {
    @Autowired
    private WebApplicationContext context;

    private static final String SWAGGER_JSON_FILE = "target/swagger.json";

    @Test
    public void generateSwagger() throws Exception {
        var mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andDo(result -> FileUtils.writeStringToFile(new File(SWAGGER_JSON_FILE),
                        result.getResponse().getContentAsString(), StandardCharsets.UTF_8));

        Assertions.assertTrue(Files.exists(Path.of(SWAGGER_JSON_FILE)));
    }
}
