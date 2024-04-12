package com.example.imageproject.service;

import com.example.imageproject.config.BaseTest;
import com.example.imageproject.exception.EntityNotFoundException;
import com.example.imageproject.exception.IllegalAccessException;
import com.example.imageproject.model.entity.Image;
import com.example.imageproject.model.entity.User;
import com.example.imageproject.model.enumeration.PrivilegeEnum;
import com.example.imageproject.model.enumeration.RoleEnum;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Transactional
public class ImageServiceTest extends BaseTest {

    @Autowired
    private ImageService imageService;
    @Autowired
    private ImageRepository imageRepository;
    @MockBean
    private MinioService minioService;

    @Test
    public void saveImage_MultipartFile() {
        var userId = 1L;
        var authentication = new UsernamePasswordAuthenticationToken(new User(userId, "username",
                "password", RoleEnum.ROLE_USER, null), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var content = "content";
        var multipartFile = new MockMultipartFile("file.txt", content.getBytes());

        Mockito.doNothing().when(minioService).upload(Mockito.any(), Mockito.any());

        var response = imageService.saveImage(multipartFile);
        var image = imageService.getImageByImageId(response.getImageId());

        Assertions.assertAll(
                () -> Assertions.assertNotNull(response.getImageId()),
                () -> Assertions.assertTrue(response.getImageId().matches("[0-9a-f\\-]+")),
                () -> Assertions.assertTrue(imageRepository.existsByImageId(response.getImageId())),
                () -> Assertions.assertEquals(userId, image.getUserId()),
                () -> Assertions.assertEquals(content.getBytes().length, image.getSize())
        );
    }

    @Test
    public void saveImage_ImageIds_modifiedImageExists() {
        var imageId = "imageId";
        var image = new Image(null, "filename", 1L, imageId, 1L);
        imageRepository.save(image);

        imageService.saveImage(imageId, imageId);

        Mockito.verify(minioService, Mockito.never()).download(Mockito.any());
    }

    @Test
    public void saveImage_ImageIds_modifiedImageNotExists() {
        var imageId = "imageId";
        var imageSize = 1;
        var userId = 1L;
        var image = new Image(null, "filename", (long) imageSize, imageId, userId);
        imageRepository.save(image);

        var modifiedId = "newImage";
        Mockito.when(minioService.download(Mockito.any())).thenReturn(new byte[imageSize]);

        var modified = imageService.saveImage(imageId, modifiedId);

        Mockito.verify(minioService, Mockito.times(1)).download(Mockito.any());

        Assertions.assertAll(
                () -> Assertions.assertTrue(imageRepository.existsByImageId(modifiedId)),
                () -> Assertions.assertEquals(modifiedId, modified.getImageId()),
                () -> Assertions.assertEquals(imageSize, modified.getSize()),
                () -> Assertions.assertEquals(userId, modified.getUserId())
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

        var imageId = "imageId";
        var userId = 1L;
        var image = new Image(null, "filename", 1L, imageId, userId);
        imageRepository.save(image);

        var result = imageService.getAllMeta();

        Assertions.assertAll(
                () -> Assertions.assertFalse(result.isEmpty()),
                () -> Assertions.assertEquals(imageId, result.get(0).getImageId())
        );
    }

    @Test
    public void getAllMeta_NoFullAccess_HasImages() {
        var userId = 1L;
        var authentication = new UsernamePasswordAuthenticationToken(new User(userId, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "imageId";
        var image = new Image(null, "filename", 1L, imageId, userId);
        imageRepository.save(image);

        var result = imageService.getAllMeta();

        Assertions.assertAll(
                () -> Assertions.assertFalse(result.isEmpty()),
                () -> Assertions.assertEquals(imageId, result.get(0).getImageId())
        );
    }

    @Test
    public void getAllMeta_NoFullAccess_HasNoImages() {
        var userId = 1L;
        var authentication = new UsernamePasswordAuthenticationToken(new User(userId, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var result = imageService.getAllMeta();

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void downloadFile_HasAccess() {
        var user = new User();
        user.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority(PrivilegeEnum.IMAGE_FULL_ACCESS_PRIVILEGE.name())
        ));
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "imageId";
        var image = new Image(null, "filename.jpeg", 1L, imageId, 1L);
        imageRepository.save(image);

        Mockito.when(minioService.download(Mockito.any())).thenReturn(new byte[1]);

        var response = imageService.download(imageId);

        Assertions.assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
    }

    @Test
    public void downloadFile_HasNoAccess() {
        var authentication = new UsernamePasswordAuthenticationToken(new User(2L, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "imageId";
        var image = new Image(null, "filename.jpeg", 1L, imageId, 1L);
        imageRepository.save(image);

        Mockito.when(minioService.download(Mockito.any())).thenReturn(new byte[1]);

        Assertions.assertThrows(IllegalAccessException.class, () -> imageService.download(imageId));
    }

    @Test
    public void downloadFile_NoFile() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> imageService.download("imageId"));
    }

    @Test
    public void deleteFile_HasAccess() {
        var user = new User();
        user.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority(PrivilegeEnum.IMAGE_FULL_ACCESS_PRIVILEGE.name())
        ));
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "imageId";
        var image = new Image(null, "filename.jpeg", 1L, imageId, 1L);
        imageRepository.save(image);

        Mockito.doNothing().when(minioService).delete(Mockito.any());

        var response = imageService.delete(imageId);

        Assertions.assertAll(
                () -> Assertions.assertFalse(imageRepository.existsByImageId(imageId)),
                () -> Assertions.assertTrue(response.getSuccess()),
                () -> Assertions.assertEquals("The image was successfully deleted", response.getMessage())
        );
    }

    @Test
    public void deleteFile_HasNoAccess() {
        var authentication = new UsernamePasswordAuthenticationToken(new User(2L, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "imageId";
        var image = new Image(null, "filename.jpeg", 1L, imageId, 1L);
        imageRepository.save(image);

        Mockito.doNothing().when(minioService).delete(Mockito.any());

        Assertions.assertThrows(IllegalAccessException.class, () -> imageService.delete(imageId));
    }

    @Test
    public void deleteFile_NoFile() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> imageService.delete("imageId"));
    }

    @Test
    public void validateAccess_HasAccess() {
        var userId = 1L;
        var authentication = new UsernamePasswordAuthenticationToken(new User(userId, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "imageId";
        var image = new Image(null, "filename.jpeg", 1L, imageId, userId);
        imageRepository.save(image);

        Assertions.assertTrue(imageService.validateAccess(imageId));
    }

    @Test
    public void validateAccess_HasNoAccess() {
        var authentication = new UsernamePasswordAuthenticationToken(new User(2L, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "imageId";
        var image = new Image(null, "filename.jpeg", 1L, imageId, 1L);
        imageRepository.save(image);

        Assertions.assertFalse(imageService.validateAccess(imageId));
    }

    @Test
    public void validateAccess_HasFullAccess() {
        var user = new User();
        user.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority(PrivilegeEnum.IMAGE_FULL_ACCESS_PRIVILEGE.name())
        ));
        var authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "imageId";
        var image = new Image(null, "filename.jpeg", 1L, imageId, 1L);
        imageRepository.save(image);

        Assertions.assertTrue(imageService.validateAccess(imageId));
    }

    @Test
    public void validateAccess_NoFile() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> imageService.download("imageId"));
    }

    @Test
    public void getImageByImageId_ImageExists() {
        var imageId = "imageId";
        var image = new Image(null, "filename.jpeg", 1L, imageId, 1L);
        image = imageRepository.save(image);

        var result = imageService.getImageByImageId(imageId);

        Assertions.assertEquals(image, result);
    }

    @Test
    public void getImageByImageId_ImageNotExists() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> imageService.getImageByImageId("imageId"));
    }

}
