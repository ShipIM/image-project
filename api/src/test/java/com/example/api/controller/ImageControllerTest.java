package com.example.api.controller;

import com.example.api.config.BaseTest;
import com.example.api.dto.rest.error.UiSuccessContainer;
import com.example.api.dto.rest.image.UploadImageResponse;
import com.example.api.exception.EntityNotFoundException;
import com.example.api.service.ImageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

public class ImageControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @Test
    @WithMockUser(authorities = "IMAGE_UPLOAD_PRIVILEGE")
    public void uploadImage_Success() throws Exception {
        var response = new UploadImageResponse();
        response.setImageId("imageId");

        Mockito.when(imageService.saveImage(Mockito.any())).thenReturn(response);

        var file = new MockMultipartFile("file", "file.jpeg",
                MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/image")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageId").value("imageId"));
    }

    @Test
    @WithMockUser(authorities = "IMAGE_UPLOAD_PRIVILEGE")
    public void uploadImage_UnsupportedFormat() throws Exception {
        var file = new MockMultipartFile("file", "file.txt",
                MediaType.TEXT_PLAIN_VALUE, "content".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/image")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "IMAGE_UPLOAD_PRIVILEGE")
    public void uploadImage_TooBigImageSize() throws Exception {
        var file = new MockMultipartFile("file", "file.txt",
                MediaType.TEXT_PLAIN_VALUE, new byte[10485761]);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/image")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void uploadImage_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/image"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "IMAGE_DOWNLOAD_PRIVILEGE")
    public void downloadImage_Success() throws Exception {
        var content = "content".getBytes();
        var response = ResponseEntity.ok()
                .body((Resource) new ByteArrayResource(content));
        Mockito.when(imageService.download("imageId")).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/image/imageId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().bytes(content));
    }

    @Test
    @WithMockUser(authorities = "IMAGE_DOWNLOAD_PRIVILEGE")
    public void downloadImage_NotFound() throws Exception {
        Mockito.when(imageService.download(Mockito.any())).thenThrow(new EntityNotFoundException(""));

        mockMvc.perform(MockMvcRequestBuilders.get("/image/nonExistingImageId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void downloadImage_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/image/nonExistingImageId"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "IMAGE_DELETE_PRIVILEGE")
    public void deleteImage_Success() throws Exception {
        Mockito.when(imageService.delete("imageId"))
                .thenReturn(new UiSuccessContainer(true, "Image deleted successfully"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/image/imageId"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success")
                        .value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Image deleted successfully"));
    }

    @Test
    @WithMockUser(authorities = "IMAGE_DELETE_PRIVILEGE")
    public void deleteImage_NotFound() throws Exception {
        Mockito.when(imageService.delete(Mockito.any())).thenThrow(new EntityNotFoundException(""));

        mockMvc.perform(MockMvcRequestBuilders.delete("/image/nonExistingImageId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void deleteImage_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/image/imageId"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "IMAGE_READ_PRIVILEGE")
    public void getImages_Success() throws Exception {
        Mockito.when(imageService.getAllMeta()).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/images")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getImages_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/images"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

}
