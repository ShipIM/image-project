package com.example.imageapi.dto.mapper;

import com.example.imageapi.dto.kafka.image.ImageFilterRequest;
import com.example.imageapi.model.entity.FilterRequest;
import com.example.imageapi.model.entity.Image;
import com.example.imageapi.model.enumeration.FilterType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FilterMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "originalId", source = "image.imageId")
    FilterRequest toFilterRequest(Image image, String requestId);

    @Mapping(target = "imageId", source = "filterRequest.originalId")
    ImageFilterRequest toImageFilter(FilterRequest filterRequest, List<FilterType> filters);

}
