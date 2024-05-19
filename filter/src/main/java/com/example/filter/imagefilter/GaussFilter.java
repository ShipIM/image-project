package com.example.filter.imagefilter;

import com.example.filter.api.imagefilter.ConcreteImageFilter;
import com.example.filter.exception.ConversionFailedException;
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

@Profile(value = "gauss")
@Component
@Slf4j
public class GaussFilter extends ConcreteImageFilter {

    private final Double SIGMA = 3.;

    public GaussFilter() {
        super(FilterType.GAUSS);
    }

    public byte[] convert(byte[] imageBytes) throws ConversionFailedException {
        try {
            var inputStream = new ByteArrayInputStream(imageBytes);
            var imageInputStream = ImageIO.createImageInputStream(inputStream);

            var reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream, true);
            var formatName = reader.getFormatName();

            var originalImage = ImageIO.read(imageInputStream);

            var width = originalImage.getWidth();
            var height = originalImage.getHeight();

            var blurredImage = new BufferedImage(width, height, originalImage.getType());

            var forkJoinPool = ForkJoinPool.commonPool();
            var convertTask = new BlurringTask(originalImage, blurredImage, 0, 0, width, height,
                    createGaussianKernel(SIGMA));
            forkJoinPool.invoke(convertTask);

            var outputStream = new ByteArrayOutputStream();
            ImageIO.write(blurredImage, formatName, outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Unable to perform image conversion, an error occurred: {}", e.getMessage(), e);

            throw new ConversionFailedException("Unable to apply Gauss filter, an error occurred");
        }
    }

    private double[][] createGaussianKernel(double sigma) {
        var kernelSize = (int) Math.ceil(6 * SIGMA);
        kernelSize += kernelSize % 2 == 0 ? 1 : 0;

        var kernel = new double[kernelSize][kernelSize];

        var center = kernelSize / 2.;
        var sum = 0.;

        for (var i = 0; i < kernelSize; i++) {
            for (var j = 0; j < kernelSize; j++) {
                var x = i - center;
                var y = j - center;

                var normalizationCoefficient = 1. / (2 * Math.PI * sigma * sigma);
                var exponent = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                var value = normalizationCoefficient * exponent;

                kernel[i][j] = value;

                sum += value;
            }
        }

        for (var i = 0; i < kernelSize; i++) {
            for (var j = 0; j < kernelSize; j++) {
                kernel[i][j] /= sum;
            }
        }

        return kernel;
    }

    @RequiredArgsConstructor
    private static class BlurringTask extends RecursiveAction {

        private static final Integer THRESHOLD = 10000;

        private final BufferedImage inputImage;
        private final BufferedImage outputImage;

        private final Integer startX;
        private final Integer startY;

        private final Integer width;
        private final Integer height;

        private final double[][] kernel;

        @Override
        public void compute() {
            var totalPixels = width * height;

            if (totalPixels <= THRESHOLD) {
                applyKernel();
            } else {
                var midHeight = height / 2;

                var upperHalfTask = new BlurringTask(inputImage, outputImage, startX, startY, width,
                        midHeight, kernel);
                var lowerHalfTask = new BlurringTask(inputImage, outputImage, startX,
                        startY + midHeight, width, height - midHeight, kernel);

                invokeAll(upperHalfTask, lowerHalfTask);
            }
        }

        private void applyKernel() {
            var kernelRadius = kernel.length / 2;

            for (var y = startY; y < startY + height; y++) {
                for (var x = startX; x < startX + width; x++) {
                    var redSum = 0.;
                    var greenSum = 0.;
                    var blueSum = 0.;
                    var weightSum = 0.;

                    for (var ky = -kernelRadius; ky <= kernelRadius; ky++) {
                        for (var kx = -kernelRadius; kx <= kernelRadius; kx++) {
                            var px = Math.min(Math.max(x + kx, 0), inputImage.getWidth() - 1);
                            var py = Math.min(Math.max(y + ky, 0), inputImage.getHeight() - 1);

                            var rgb = inputImage.getRGB(px, py);
                            var red = (rgb >> 16) & 0xFF;
                            var green = (rgb >> 8) & 0xFF;
                            var blue = rgb & 0xFF;

                            var weight = kernel[ky + kernelRadius][kx + kernelRadius];

                            redSum += red * weight;
                            greenSum += green * weight;
                            blueSum += blue * weight;

                            weightSum += weight;
                        }
                    }

                    var newRed = (int) Math.round((redSum / weightSum));
                    var newGreen = (int) Math.round((greenSum / weightSum));
                    var newBlue = (int) Math.round((blueSum / weightSum));

                    var newRgb = (newRed << 16) | (newGreen << 8) | newBlue;
                    outputImage.setRGB(x, y, newRgb);
                }
            }
        }
    }

}
