package com.example.imageproject.service;

import com.example.imageproject.config.BaseTest;
import com.example.imageproject.exception.EntityNotFoundException;
import com.example.imageproject.exception.IllegalAccessException;
import com.example.imageproject.model.entity.Image;
import com.example.imageproject.model.entity.User;
import com.example.imageproject.model.enumeration.PrivilegeEnum;
import com.example.imageproject.repository.ImageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

public class ImageServiceTest extends BaseTest {

    @Autowired
    private ImageService imageService;
    @MockBean
    private ImageRepository imageRepository;
    @MockBean
    private MinioService minioService;

    @Test
    public void saveImage() {
        var authentication = new UsernamePasswordAuthenticationToken(new User(), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var multipartFile = new MockMultipartFile("file.txt", "content".getBytes());

        Mockito.when(imageRepository.save(Mockito.any())).thenReturn(new Image());
        Mockito.doNothing().when(minioService).upload(Mockito.any(), Mockito.any());

        var response = imageService.saveImage(multipartFile);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(response.getImageId()),
                () -> Assertions.assertTrue(response.getImageId().matches("[0-9a-f\\-]+"))
        );
    }

    @Test
    public void getAllMeta_FullAccess() {
        var user = new User();
        user.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority(PrivilegeEnum.IMAGE_FULL_ACCESS_PRIVILEGE.name())
        ));
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(imageRepository.findAll()).thenReturn(Collections.emptyList());
        Mockito.when(imageRepository.findAllByUserId(Mockito.any())).thenReturn(Collections.emptyList());

        imageService.getAllMeta();

        Mockito.verify(imageRepository, Mockito.times(1)).findAll();
        Mockito.verify(imageRepository, Mockito.never()).findAllByUserId(Mockito.any());
    }

    @Test
    public void getAllMeta_NoFullAccess() {
        var user = new User();
        user.setAuthorities(Collections.emptyList());
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(imageRepository.findAll()).thenReturn(Collections.emptyList());
        Mockito.when(imageRepository.findAllByUserId(Mockito.any())).thenReturn(Collections.emptyList());

        imageService.getAllMeta();

        Mockito.verify(imageRepository, Mockito.never()).findAll();
        Mockito.verify(imageRepository, Mockito.times(1)).findAllByUserId(Mockito.any());
    }

    @Test
    public void downloadFile_FullAccess() {
        var user = new User();
        user.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority(PrivilegeEnum.IMAGE_FULL_ACCESS_PRIVILEGE.name())
        ));
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var retrievedImage = new Image();
        retrievedImage.setUserId(-1L);
        retrievedImage.setFilename("file.jpeg");

        Mockito.when(imageRepository.findUserByImageId(Mockito.any())).thenReturn(Optional.of(-2L));
        Mockito.when(imageRepository.findByImageId(Mockito.any())).thenReturn(Optional.of(retrievedImage));
        Mockito.when(minioService.download(Mockito.any())).thenReturn("content".getBytes());

        var response = imageService.download(Mockito.any());

        Assertions.assertEquals(response.getHeaders().getContentType(), MediaType.IMAGE_JPEG);
    }

    @Test
    public void downloadFile_HasAccess() {
        var user = new User();
        user.setAuthorities(Collections.emptyList());
        user.setId(-1L);
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var retrievedImage = new Image();
        retrievedImage.setUserId(-1L);
        retrievedImage.setFilename("file.png");

        Mockito.when(imageRepository.findUserByImageId(Mockito.any())).thenReturn(Optional.of(-1L));
        Mockito.when(imageRepository.findByImageId(Mockito.any())).thenReturn(Optional.of(retrievedImage));
        Mockito.when(minioService.download(Mockito.any())).thenReturn("content".getBytes());

        var response = imageService.download(Mockito.any());

        Assertions.assertEquals(response.getHeaders().getContentType(), MediaType.IMAGE_PNG);
    }

    @Test
    public void downloadFile_NoAccess() {
        var user = new User();
        user.setId(-1L);
        user.setAuthorities(Collections.emptyList());
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(imageRepository.findUserByImageId(Mockito.any())).thenReturn(Optional.of(-2L));

        Assertions.assertThrows(IllegalAccessException.class, () -> imageService.download(Mockito.any()));
    }

    @Test
    public void downloadFile_NoFile() {
        Mockito.when(imageRepository.findUserByImageId(Mockito.any())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> imageService.download(Mockito.any()));
    }

    @Test
    public void deleteFile_FullAccess() {
        var user = new User();
        user.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority(PrivilegeEnum.IMAGE_FULL_ACCESS_PRIVILEGE.name())
        ));
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(imageRepository.findUserByImageId(Mockito.any())).thenReturn(Optional.of(-1L));
        Mockito.doNothing().when(minioService).delete(Mockito.any());

        var response = imageService.delete(Mockito.any());

        Assertions.assertAll(
                () -> Assertions.assertTrue(response.getSuccess()),
                () -> Assertions.assertEquals(response.getMessage(), "The image was successfully deleted")
        );
    }

    @Test
    public void deleteFile_HasAccess() {
        var user = new User();
        user.setAuthorities(Collections.emptyList());
        user.setId(-1L);
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(imageRepository.findUserByImageId(Mockito.any())).thenReturn(Optional.of(-1L));
        Mockito.doNothing().when(minioService).delete(Mockito.any());

        var response = imageService.delete(Mockito.any());

        Assertions.assertAll(
                () -> Assertions.assertTrue(response.getSuccess()),
                () -> Assertions.assertEquals(response.getMessage(), "The image was successfully deleted")
        );
    }

    @Test
    public void deleteFile_NoAccess() {
        var user = new User();
        user.setId(-1L);
        user.setAuthorities(Collections.emptyList());
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(imageRepository.findUserByImageId(Mockito.any())).thenReturn(Optional.of(-2L));

        Assertions.assertThrows(IllegalAccessException.class, () -> imageService.delete(Mockito.any()));
    }

    @Test
    public void deleteFile_NoFile() {
        Mockito.when(imageRepository.findByImageId(Mockito.any())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> imageService.delete(Mockito.any()));
    }

}
