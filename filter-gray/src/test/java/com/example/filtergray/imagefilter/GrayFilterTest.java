package com.example.filtergray.imagefilter;

import com.example.filtergray.config.BaseTest;
import com.example.filtergray.model.enumeration.FilterType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class GrayFilterTest extends BaseTest {

    @Autowired
    private GrayFilter grayFilter;

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

        var grayscaleImageBytes = grayFilter.convert(colorImageBytes);
        var expectedGrayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        expectedGrayscaleImage.setRGB(0, 0, new Color(75, 75, 75).getRGB());
        expectedGrayscaleImage.setRGB(0, 1, new Color(149, 149, 149).getRGB());
        expectedGrayscaleImage.setRGB(0, 2, new Color(28, 28, 28).getRGB());
        expectedGrayscaleImage.setRGB(1, 0, new Color(225, 225, 225).getRGB());
        expectedGrayscaleImage.setRGB(1, 1, new Color(105, 105, 105).getRGB());
        expectedGrayscaleImage.setRGB(1, 2, new Color(178, 178, 178).getRGB());
        expectedGrayscaleImage.setRGB(2, 0, new Color(127, 127, 127).getRGB());
        expectedGrayscaleImage.setRGB(2, 1, new Color(255, 255, 255).getRGB());
        expectedGrayscaleImage.setRGB(2, 2, new Color(0, 0, 0).getRGB());

        var expectedGrayscaleOutputStream = new ByteArrayOutputStream();
        ImageIO.write(expectedGrayscaleImage, "png", expectedGrayscaleOutputStream);
        var expectedGrayscaleImageBytes = expectedGrayscaleOutputStream.toByteArray();

        Assertions.assertArrayEquals(expectedGrayscaleImageBytes, grayscaleImageBytes);
    }

    @Test
    public void getFilterType() {
        Assertions.assertEquals(FilterType.GRAY, grayFilter.getFilterType());
    }

}
