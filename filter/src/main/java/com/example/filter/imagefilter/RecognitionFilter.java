package com.example.filter.imagefilter;

import com.example.filter.api.imagefilter.ConcreteImageFilter;
import com.example.filter.exception.ConversionFailedException;
import com.example.filter.model.enumeration.FilterType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Profile(value = "recognition")
@Component
@Slf4j
public class RecognitionFilter extends ConcreteImageFilter {

    private static final Font FONT = new Font("Arial", Font.BOLD, 18);
    private static final Color COLOR = Color.BLACK;
    private static final Integer TAGS_LIMIT = 3;

    private final RestClient restClient;

    public RecognitionFilter(RestClient restClient) {
        super(FilterType.RECOGNITION);

        this.restClient = restClient;
    }

    public byte[] convert(byte[] imageBytes) throws ConversionFailedException {
        try {
            var map = new LinkedMultiValueMap<String, byte[]>();
            map.add("image", imageBytes);

            var uploadsResponse = restClient.post()
                    .uri("/uploads")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(map)
                    .retrieve()
                    .body(UploadsResponse.class);
            if (Objects.isNull(uploadsResponse)) {
                throw new RuntimeException("uploads response is null, can't process");
            } else if (uploadsResponse.getStatus().type.equals("error")) {
                throw new RuntimeException(uploadsResponse.status.text);
            }

            var tagsResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/tags")
                            .queryParam("image_upload_id", uploadsResponse.result.uploadId)
                            .build())
                    .retrieve()
                    .body(TagsResponse.class);
            if (Objects.isNull(tagsResponse)) {
                throw new RuntimeException("tags response is null, can't process");
            }
            var text = tagsResponse.result.tags.stream()
                    .sorted(Comparator.comparingDouble(a -> -1 * a.confidence))
                    .limit(TAGS_LIMIT)
                    .map(tag -> tag.tag.en)
                    .collect(Collectors.joining(", "));

            return applyText(imageBytes, text);
        } catch (Exception e) {
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

        var fontSize = image.getHeight() * 0.05f;
        var suitableFont = FONT.deriveFont(FONT.getStyle(), fontSize);

        graphics.setFont(suitableFont);
        graphics.setColor(COLOR);

        var metrics = graphics.getFontMetrics(suitableFont);
        var positionX = (int) (image.getWidth() * 0.02);
        var positionY = (int) (image.getHeight() - metrics.getDescent() - image.getHeight() * 0.02);

        graphics.drawString(text, positionX, positionY);

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, outputStream);

        return outputStream.toByteArray();
    }

    @Data
    private static class UploadsResponse {

        private UploadsResult result;

        private ResponseStatus status;

        @Data
        private static class UploadsResult {

            @JsonProperty("upload_id")
            private String uploadId;

        }

    }

    @Data
    private static class TagsResponse {

        private TagsResult result;

        private ResponseStatus status;

        @Data
        private static class TagsResult {

            private List<Tag> tags;

        }

        @Data
        private static class Tag {

            private Double confidence;

            private TagDetail tag;

        }

        @Data
        private static class TagDetail {

            private String en;

        }

    }

    @Data
    private static class ResponseStatus {

        private String text;

        private String type;

    }

}

