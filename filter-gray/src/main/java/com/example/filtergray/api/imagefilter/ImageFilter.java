package com.example.filtergray.api.imagefilter;

import com.example.filtergray.model.enumeration.FilterType;

import java.io.IOException;

public interface ImageFilter {

    byte[] convert(byte[] original) throws IOException;

    FilterType getFilterType();

}
