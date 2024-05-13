package com.example.filter.api.imagefilter;

import com.example.filter.model.enumeration.FilterType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class ConcreteImageFilter implements ImageFilter {

    private final FilterType filterType;

}
