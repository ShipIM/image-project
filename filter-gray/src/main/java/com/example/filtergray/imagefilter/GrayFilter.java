package com.example.filtergray.imagefilter;

import com.example.filtergray.api.imagefilter.ConcreteImageFilter;
import com.example.filtergray.model.enumeration.FilterType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@Component
public class GrayFilter extends ConcreteImageFilter {

    public GrayFilter() {
        super(FilterType.GRAY);
    }

    @Override
    public byte[] convert(byte[] imageBytes) throws IOException {
        var inputStream = new ByteArrayInputStream(imageBytes);
        var imageInputStream = ImageIO.createImageInputStream(inputStream);

        var reader = ImageIO.getImageReaders(imageInputStream).next();
        reader.setInput(imageInputStream, true);
        var formatName = reader.getFormatName();

        var colorImage = ImageIO.read(imageInputStream);

        var width = colorImage.getWidth();
        var height = colorImage.getHeight();

        var grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        var forkJoinPool = new ForkJoinPool();
        var convertTask = new ConvertToGrayscaleTask(colorImage, grayscaleImage, 0, 0, width, height);
        forkJoinPool.invoke(convertTask);

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(grayscaleImage, formatName, outputStream);

        return outputStream.toByteArray();
    }

    @RequiredArgsConstructor
    private static class ConvertToGrayscaleTask extends RecursiveAction {
        private static final int THRESHOLD = 10000;

        private static final Double RED_WEIGHT = 0.299;
        private static final Double GREEN_WEIGHT = 0.587;
        private static final Double BLUE_WEIGHT = 0.114;

        private final BufferedImage colorImage;
        private final BufferedImage grayscaleImage;

        private final int startX;
        private final int startY;

        private final int width;
        private final int height;

        @Override
        protected void compute() {
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
