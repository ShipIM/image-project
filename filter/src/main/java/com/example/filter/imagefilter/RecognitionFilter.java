package com.example.filter.imagefilter;

import com.example.filter.api.imagefilter.ConcreteImageFilter;
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
import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
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

    public byte[] convert(byte[] imageBytes) {
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

            System.out.println(uploadsResponse.result.uploadId);

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
        }

        return imageBytes;
    }

    private byte[] applyText(byte[] imageBytes, String text) throws IOException {
        var inputStream = new ByteArrayInputStream(imageBytes);
        var imageInputStream = ImageIO.createImageInputStream(inputStream);

        var reader = ImageIO.getImageReaders(imageInputStream).next();
        reader.setInput(imageInputStream, true);
        var formatName = reader.getFormatName();

        var image = ImageIO.read(imageInputStream);
        var graphics = image.getGraphics();

        var attributedText = new AttributedString(text);
        attributedText.addAttribute(TextAttribute.FOREGROUND, COLOR);

        var metrics = graphics.getFontMetrics(FONT);
        var positionX = (image.getWidth() - metrics.stringWidth(text)) / 2;
        var positionY = (image.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

        var ruler = graphics.getFontMetrics(FONT);
        var vector = FONT.createGlyphVector(ruler.getFontRenderContext(), text);

        var outline = vector.getOutline(0, 0);

        var expectedWidth = outline.getBounds().getWidth();
        var expectedHeight = outline.getBounds().getHeight();

        var widthBasedFontSize = (FONT.getSize2D() * image.getWidth()) / expectedWidth;
        var heightBasedFontSize = (FONT.getSize2D() * image.getHeight()) / expectedHeight;

        var newFontSize = Math.min(widthBasedFontSize, heightBasedFontSize);
        var suitableFont = FONT.deriveFont(FONT.getStyle(), (float) newFontSize);

        attributedText.addAttribute(TextAttribute.FONT, suitableFont);

        graphics.drawString(attributedText.getIterator(), positionX, positionY);

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
    public static class TagsResponse {

        private TagsResult result;

        private ResponseStatus status;

        @Data
        public static class TagsResult {

            private List<Tag> tags;

        }

        @Data
        public static class Tag {

            private double confidence;

            private TagDetail tag;

        }

        @Data
        public static class TagDetail {

            private String en;

        }

    }

    @Data
    private static class ResponseStatus {

        private String text;

        private String type;

    }

}

