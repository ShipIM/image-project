package com.example.filter.api.imagefilter;

import com.example.filter.exception.ConversionFailedException;

public interface ImageFilter {

    byte[] convert(byte[] original) throws ConversionFailedException;

}
