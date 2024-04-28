package com.example.filter.imagefilter;

import com.example.filter.api.imagefilter.ConcreteImageFilter;
import com.example.filter.model.enumeration.FilterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@Profile(value = "threshold")
@Component
@Slf4j
public class ThresholdFilter extends ConcreteImageFilter {

    public ThresholdFilter() {
        super(FilterType.THRESHOLD);
    }

    public byte[] convert(byte[] imageBytes) {
        try {
            var inputStream = new ByteArrayInputStream(imageBytes);
            var imageInputStream = ImageIO.createImageInputStream(inputStream);

            var reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream, true);
            var formatName = reader.getFormatName();

            var originalImage = ImageIO.read(imageInputStream);

            var width = originalImage.getWidth();
            var height = originalImage.getHeight();

            var thresholdImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

            var forkJoinPool = ForkJoinPool.commonPool();
            var convertTask = new ThresholdingTask(originalImage, thresholdImage, 0, 0, width, height);
            forkJoinPool.invoke(convertTask);

            var outputStream = new ByteArrayOutputStream();
            ImageIO.write(thresholdImage, formatName, outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Unable to perform image conversion, an error occurred: {}", e.getMessage(), e);
        }

        return imageBytes;
    }

    @RequiredArgsConstructor
    private static class ThresholdingTask extends RecursiveAction {

        private static final Integer CHANNEL_THRESHOLD = 128;

        private static final Integer THRESHOLD = 10000;

        private final BufferedImage inputImage;
        private final BufferedImage outputImage;

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

                var upperHalfTask = new ThresholdingTask(inputImage, outputImage, startX, startY, width,
                        midHeight);
                var lowerHalfTask = new ThresholdingTask(inputImage, outputImage, startX,
                        startY + midHeight, width, height - midHeight);

                invokeAll(upperHalfTask, lowerHalfTask);
            }
        }

        private void processPixels() {
            for (var y = startY; y < startY + height; y++) {
                for (var x = startX; x < startX + width; x++) {
                    var rgb = inputImage.getRGB(x, y);

                    var red = (rgb >> 16) & 0xFF;
                    var green = (rgb >> 8) & 0xFF;
                    var blue = rgb & 0xFF;

                    if (red >= CHANNEL_THRESHOLD || green >= CHANNEL_THRESHOLD || blue >= CHANNEL_THRESHOLD) {
                        outputImage.setRGB(x, y, 0xFFFFFF);
                    } else {
                        outputImage.setRGB(x, y, 0x000000);
                    }
                }
            }
        }
    }

}
