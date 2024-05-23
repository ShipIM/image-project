package com.example.filter.imagefilter;

import com.example.filter.api.imagefilter.ConcreteImageFilter;
import com.example.filter.exception.ConversionFailedException;
import com.example.filter.exception.RetryableException;
import com.example.filter.model.enumeration.FilterType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.bucket4j.Bucket;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Profile(value = "recognition")
@Component
@Slf4j
public class RecognitionFilter extends ConcreteImageFilter {

    @Value("${filter.tags-limit}")
    private Integer TAGS_LIMIT = 3;

    private final RestClient restClient;

    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final Bucket bucket;

    public RecognitionFilter(RestClient restClient, Retry retry, CircuitBreaker circuitBreaker, Bucket bucket) {
        super(FilterType.RECOGNITION);

        this.restClient = restClient;

        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
        this.bucket = bucket;
    }

    public byte[] convert(byte[] imageBytes) throws ConversionFailedException {
        if (!bucket.tryConsume(1)) {
            throw new ConversionFailedException("Too many requests to target service per period");
        }

        try {
            var map = new LinkedMultiValueMap<String, byte[]>();
            map.add("image", imageBytes);

            var uploadsResponse = retry.executeSupplier(() ->
                    circuitBreaker.executeSupplier(() ->
                            restClient.post()
                                    .uri("/uploads")
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .body(map)
                                    .retrieve()
                                    .onStatus(httpStatusCode ->
                                                    httpStatusCode.equals(HttpStatus.TOO_MANY_REQUESTS) ||
                                                            httpStatusCode.is5xxServerError(),
                                            (request, response) -> {
                                                throw new RetryableException(response.getStatusText());
                                            })
                                    .body(UploadsResponse.class)));

            var tagsResponse = retry.executeSupplier(() ->
                    circuitBreaker.executeSupplier(() ->
                            restClient.get()
                                    .uri(uriBuilder -> uriBuilder.path("/tags")
                                            .queryParam("image_upload_id", uploadsResponse.result.uploadId)
                                            .build())
                                    .retrieve()
                                    .onStatus(httpStatusCode ->
                                                    httpStatusCode.equals(HttpStatus.TOO_MANY_REQUESTS) ||
                                                            httpStatusCode.is5xxServerError(),
                                            (request, response) -> {
                                                throw new RetryableException(response.getStatusText());
                                            })
                                    .body(TagsResponse.class)));

            var text = tagsResponse.result.tags.stream()
                    .sorted(Comparator.comparingDouble(a -> -1 * a.confidence))
                    .limit(TAGS_LIMIT)
                    .map(tag -> tag.tag.en)
                    .collect(Collectors.joining(", "));

            return applyText(imageBytes, text);
        } catch (RetryableException | CallNotPermittedException | IOException e) {
            log.error("Unable to perform image conversion, an error occurred: {}", e.getMessage(), e);

            throw new ConversionFailedException("Unable to apply Recognition filter, an error occurred");
        }
    }

    private byte[] applyText(byte[] imageBytes, String text) throws IOException {
        var inputStream = new ByteArrayInputStream(imageBytes);
        var imageInputStream = ImageIO.createImageInputStream(inputStream);

        var reader = ImageIO.getImageReaders(imageInputStream).next();
        reader.setInput(imageInputStream, true);
        var formatName = reader.getFormatName();

        var image = ImageIO.read(imageInputStream);
        var graphics = image.getGraphics();

        var font = new Font("Arial", Font.BOLD, 18);

        var fontSize = image.getWidth() * 0.05f;
        var suitableFont = font.deriveFont(font.getStyle(), fontSize);
        graphics.setFont(suitableFont);

        var metrics = graphics.getFontMetrics(suitableFont);
        var textWidth = metrics.stringWidth(text);

        while (textWidth > image.getWidth() * 0.96f) {
            fontSize *= 0.95;
            suitableFont = font.deriveFont(font.getStyle(), fontSize);

            graphics.setFont(suitableFont);
            metrics = graphics.getFontMetrics(suitableFont);
            textWidth = metrics.stringWidth(text);
        }

        var positionX = (int) (image.getWidth() * 0.02);
        var positionY = (int) (image.getHeight() - metrics.getDescent() - image.getHeight() * 0.02);

        graphics.setFont(suitableFont);
        graphics.setColor(Color.BLACK);

        graphics.drawString(text, positionX, positionY);

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, outputStream);

        return outputStream.toByteArray();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UploadsResponse {

        private UploadsResult result;

        private ResponseStatus status;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class UploadsResult {

            @JsonProperty("upload_id")
            private String uploadId;

        }

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TagsResponse {

        private TagsResult result;

        private ResponseStatus status;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class TagsResult {

            private List<Tag> tags;

        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Tag {

            private Double confidence;

            private TagDetail tag;

        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class TagDetail {

            private String en;

        }

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseStatus {

        private String text;

        private String type;

    }

}

