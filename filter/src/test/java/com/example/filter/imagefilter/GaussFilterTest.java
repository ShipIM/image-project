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

public class GaussFilterTest extends BaseTest {

    @Autowired
    private GaussFilter gaussFilter;

    @Test
    void convert() throws Exception {
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

        var originalOutputStream = new ByteArrayOutputStream();
        ImageIO.write(colorImage, "png", originalOutputStream);
        var colorImageBytes = originalOutputStream.toByteArray();

        var blurredImageBytes = gaussFilter.convert(colorImageBytes);
        var expectedBlurredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        expectedBlurredImage.setRGB(0, 0, new Color(121, 82, 100).getRGB());
        expectedBlurredImage.setRGB(0, 1, new Color(94, 76, 115).getRGB());
        expectedBlurredImage.setRGB(0, 2, new Color(67, 67, 128).getRGB());
        expectedBlurredImage.setRGB(1, 0, new Color(117, 90, 100).getRGB());
        expectedBlurredImage.setRGB(1, 1, new Color(92, 82, 108).getRGB());
        expectedBlurredImage.setRGB(1, 2, new Color(67, 72, 116).getRGB());
        expectedBlurredImage.setRGB(2, 0, new Color(113, 96, 100).getRGB());
        expectedBlurredImage.setRGB(2, 1, new Color(90, 85, 102).getRGB());
        expectedBlurredImage.setRGB(2, 2, new Color(66, 73, 102).getRGB());

        var expectedBlurredOutputStream = new ByteArrayOutputStream();
        ImageIO.write(expectedBlurredImage, "png", expectedBlurredOutputStream);
        var expectedBlurredImageBytes = expectedBlurredOutputStream.toByteArray();

        Assertions.assertArrayEquals(expectedBlurredImageBytes, blurredImageBytes);
    }

    @Test
    public void getFilterType() {
        Assertions.assertEquals(FilterType.GAUSS, gaussFilter.getFilterType());
    }

}
