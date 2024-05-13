package com.example.imageproject.dto.mapper;

import com.example.imageproject.dto.kafka.image.ImageFilter;
import com.example.imageproject.model.entity.FilterRequest;
import com.example.imageproject.model.entity.Image;
import com.example.imageproject.model.enumeration.FilterType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FilterMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "originalId", source = "image.imageId")
    FilterRequest toFilterRequest(Image image, String requestId);

    @Mapping(target = "imageId", source = "filterRequest.originalId")
    ImageFilter toImageFilter(FilterRequest filterRequest, List<FilterType> filters);

}
