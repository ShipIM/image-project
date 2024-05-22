package com.example.filter.imagefilter;

import com.example.filter.config.BaseTest;
import com.example.filter.model.enumeration.FilterType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ThresholdFilterTest extends BaseTest {

    @Autowired
    private ThresholdFilter thresholdFilter;

    @Test
    public void convert() throws Exception {
        var width = 3;
        var height = 3;

        var colorImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        colorImage.setRGB(0, 0, new Color(255, 0, 0).getRGB());
        colorImage.setRGB(0, 1, new Color(0, 255, 0).getRGB());
        colorImage.setRGB(0, 2, new Color(0, 0, 255).getRGB());
        colorImage.setRGB(1, 0, new Color(255, 255, 0).getRGB());
        colorImage.setRGB(1, 1, new Color(255, 0, 255).getRGB());
        colorImage.setRGB(1, 2, new Color(0, 255, 255).getRGB());
        colorImage.setRGB(2, 0, new Color(128, 128, 128).getRGB());
        colorImage.setRGB(2, 1, new Color(255, 255, 255).getRGB());
        colorImage.setRGB(2, 2, new Color(0, 0, 0).getRGB());

        var colorOutputStream = new ByteArrayOutputStream();
        ImageIO.write(colorImage, "png", colorOutputStream);
        var colorImageBytes = colorOutputStream.toByteArray();

        var binaryImageBytes = thresholdFilter.convert(colorImageBytes);

        var expectedBinaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        expectedBinaryImage.setRGB(0, 0, 0xFFFFFF);
        expectedBinaryImage.setRGB(0, 1, 0xFFFFFF);
        expectedBinaryImage.setRGB(0, 2, 0xFFFFFF);
        expectedBinaryImage.setRGB(1, 0, 0xFFFFFF);
        expectedBinaryImage.setRGB(1, 1, 0xFFFFFF);
        expectedBinaryImage.setRGB(1, 2, 0xFFFFFF);
        expectedBinaryImage.setRGB(2, 0, 0xFFFFFF);
        expectedBinaryImage.setRGB(2, 1, 0xFFFFFF);
        expectedBinaryImage.setRGB(2, 2, 0x000000);

        var expectedBinaryOutputStream = new ByteArrayOutputStream();
        ImageIO.write(expectedBinaryImage, "png", expectedBinaryOutputStream);
        var expectedBinaryImageBytes = expectedBinaryOutputStream.toByteArray();

        Assertions.assertArrayEquals(expectedBinaryImageBytes, binaryImageBytes);
    }

    @Test
    public void getFilterType() {
        Assertions.assertEquals(FilterType.THRESHOLD, thresholdFilter.getFilterType());
    }

}
