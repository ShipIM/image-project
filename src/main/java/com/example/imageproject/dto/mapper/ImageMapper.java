package com.example.imageproject.dto.mapper;

import com.example.imageproject.dto.rest.image.ImageResponse;
import com.example.imageproject.dto.rest.image.UploadImageResponse;
import com.example.imageproject.model.entity.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    @Mapping(target = "filename", expression = "java(file.getOriginalFilename())")
    Image toImage(MultipartFile file, String imageId, Long userId);

    Image toImage(String filename, Long size, String imageId, Long userId);

    ImageResponse toResponse(Image image);

    Image toImage(String reference);

    List<ImageResponse> toResponseList(List<Image> images);

    UploadImageResponse toUploadResponse(String imageId);

}
