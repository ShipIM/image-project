package com.example.filtergray.api.imagefilter;
import java.io.IOException;

public interface ImageFilter {

    byte[] convert(byte[] original) throws IOException;

}
