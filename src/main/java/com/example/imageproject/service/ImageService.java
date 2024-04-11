package com.example.imageproject.service;

import com.example.imageproject.dto.rest.error.UiSuccessContainer;
import com.example.imageproject.dto.rest.image.ImageResponse;
import com.example.imageproject.dto.rest.image.UploadImageResponse;
import com.example.imageproject.dto.mapper.ImageMapper;
import com.example.imageproject.exception.EntityNotFoundException;
import com.example.imageproject.exception.IllegalAccessException;
import com.example.imageproject.model.entity.Image;
import com.example.imageproject.model.entity.User;
import com.example.imageproject.model.enumeration.PrivilegeEnum;
import com.example.imageproject.repository.ImageRepository;
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
