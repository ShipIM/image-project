package com.example.filter.imagefilter;

import com.example.filter.api.imagefilter.ConcreteImageFilter;
import com.example.filter.exception.ConversionFailedException;
import com.example.filter.model.enumeration.FilterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@Profile(value = {"gray", "test"})
@Component
@Primary
@Slf4j
public class GrayFilter extends ConcreteImageFilter {

    @Value("${filter.threshold}")
    private Integer THRESHOLD;

    public GrayFilter() {
        super(FilterType.GRAY);
    }

    @Override
    public byte[] convert(byte[] imageBytes) throws ConversionFailedException {
        try {
            var inputStream = new ByteArrayInputStream(imageBytes);
            var imageInputStream = ImageIO.createImageInputStream(inputStream);

            var reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream, true);
            var formatName = reader.getFormatName();

            var colorImage = ImageIO.read(imageInputStream);

            var width = colorImage.getWidth();
            var height = colorImage.getHeight();

            var grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

            var forkJoinPool = ForkJoinPool.commonPool();
            var convertTask = new ConvertToGrayscaleTask(colorImage, grayscaleImage, 0, 0, width, height);
            forkJoinPool.invoke(convertTask);

            var outputStream = new ByteArrayOutputStream();
            ImageIO.write(grayscaleImage, formatName, outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Unable to perform image conversion, an error occurred: {}", e.getMessage(), e);

            throw new ConversionFailedException("Unable to apply Gray filter, an error occurred");
        }
    }

    @RequiredArgsConstructor
    private class ConvertToGrayscaleTask extends RecursiveAction {

        private static final Double RED_WEIGHT = 0.299;
        private static final Double GREEN_WEIGHT = 0.587;
        private static final Double BLUE_WEIGHT = 0.114;

        private final BufferedImage colorImage;
        private final BufferedImage grayscaleImage;

        private final Integer startX;
        private final Integer startY;

        private final Integer width;
        private final Integer height;

        @Override
        public void compute() {
            var totalPixels = width * height;

            if (totalPixels <= THRESHOLD) {
                processPixels();
            } else {
                var midHeight = height / 2;

                var upperHalfTask = new ConvertToGrayscaleTask(colorImage, grayscaleImage, startX, startY, width,
                        midHeight);
                var lowerHalfTask = new ConvertToGrayscaleTask(colorImage, grayscaleImage, startX,
                        startY + midHeight, width, height - midHeight);

                invokeAll(upperHalfTask, lowerHalfTask);
            }
        }

        private void processPixels() {
            for (var y = startY; y < startY + height; y++) {
                for (var x = startX; x < startX + width; x++) {
                    var rgb = colorImage.getRGB(x, y);
                    var r = (rgb >> 16) & 0xFF;
                    var g = (rgb >> 8) & 0xFF;
                    var b = rgb & 0xFF;

                    var gray = (int) (RED_WEIGHT * r + GREEN_WEIGHT * g + BLUE_WEIGHT * b);

                    var grayRgb = (gray << 16) | (gray << 8) | gray;
                    grayscaleImage.setRGB(x, y, grayRgb);
                }
            }
        }
    }

}
