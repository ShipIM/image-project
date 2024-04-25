package com.example.imageapi.service;

import com.example.imageapi.dto.mapper.ImageMapper;
import com.example.imageapi.dto.rest.error.UiSuccessContainer;
import com.example.imageapi.dto.rest.image.ImageResponse;
import com.example.imageapi.dto.rest.image.UploadImageResponse;
import com.example.imageapi.exception.EntityNotFoundException;
import com.example.imageapi.exception.IllegalAccessException;
import com.example.imageapi.model.entity.Image;
import com.example.imageapi.model.entity.User;
import com.example.imageapi.model.enumeration.PrivilegeEnum;
import com.example.imageapi.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MinioService minioService;
    private final ImageMapper imageMapper;

    @Transactional
    public UploadImageResponse saveImage(MultipartFile multipartFile) {
        var imageId = UUID.randomUUID().toString();
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        imageRepository.save(imageMapper.toImage(multipartFile, imageId, user.getId()));
        minioService.upload(multipartFile, imageId);

        return imageMapper.toUploadResponse(imageId);
    }

    public Image saveImage(String originalImageId, String modifiedImageId) {
        if (imageRepository.existsByImageId(modifiedImageId)) {
            return getImageByImageId(modifiedImageId);
        }

        var originalImage = getImageByImageId(originalImageId);
        var bytes = minioService.download(modifiedImageId);

        return imageRepository.save(imageMapper.toImage(originalImage.getFilename(), (long) bytes.length,
                modifiedImageId, originalImage.getUserId()));
    }

    public List<ImageResponse> getAllMeta() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var images = user.getAuthorities().stream()
                .anyMatch(authority -> Objects.equals(authority.getAuthority(),
                        PrivilegeEnum.IMAGE_FULL_ACCESS_PRIVILEGE.name())) ?
                imageRepository.findAll() : imageRepository.findAllByUserId(user.getId());

        return imageMapper.toResponseList(images);
    }

    public ResponseEntity<Resource> download(String imageId) {
        if (!validateAccess(imageId)) {
            throw new IllegalAccessException("You are not the owner of this image");
        }

        var image = getImageByImageId(imageId);

        var resource = new ByteArrayResource(minioService.download(imageId));
        var extension = image.getFilename().substring(image.getFilename().indexOf(".") + 1);

        return ResponseEntity.ok()
                .contentType(Objects.equals(extension, "png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @Transactional
    public UiSuccessContainer delete(String imageId) {
        if (!validateAccess(imageId)) {
            throw new IllegalAccessException("You are not the owner of this image");
        }

        imageRepository.removeByImageId(imageId);
        minioService.delete(imageId);

        return new UiSuccessContainer(true, "The image was successfully deleted");
    }

    public Image getImageByImageId(String imageId) {
        return imageRepository.findByImageId(imageId)
                .orElseThrow(() -> new EntityNotFoundException("There is no image with such an id"));
    }

    public boolean validateAccess(String imageId) {
        var user = imageRepository.findUserByImageId(imageId)
                .orElseThrow(() -> new EntityNotFoundException("There is no image with such an id"));
        var auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return Objects.equals(user, auth.getId()) || auth.getAuthorities().stream()
                .anyMatch(authority -> Objects.equals(authority.getAuthority(),
                        PrivilegeEnum.IMAGE_FULL_ACCESS_PRIVILEGE.name()));
    }

}
