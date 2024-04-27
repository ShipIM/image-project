package com.example.filtergray.api.imagefilter;

import com.example.filtergray.model.enumeration.FilterType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class ConcreteImageFilter implements ImageFilter {

    private final FilterType filterType;

}
