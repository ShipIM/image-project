package com.example.api.controller;

import com.example.api.config.BaseTest;
import com.example.api.dto.rest.image.ApplyImageFiltersResponse;
import com.example.api.dto.rest.image.GetModifiedImageByRequestIdResponse;
import com.example.api.exception.EntityNotFoundException;
import com.example.api.model.enumeration.FilterType;
import com.example.api.model.enumeration.ImageStatus;
import com.example.api.service.FilterRequestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class ImageFilterRequestControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilterRequestService filterRequestService;

    @Test
    @WithMockUser(authorities = "FILTER_APPLY_PRIVILEGE")
    public void applyFilter_Success() throws Exception {
        var filters = new String[]{FilterType.GRAY.name()};
        var response = new ApplyImageFiltersResponse("requestId");

        Mockito.when(filterRequestService.createRequest(Mockito.any(), Mockito.anyList())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/image/imageId/filters/apply")
                        .param("filters", filters)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.requestId").value("requestId"));
    }

    @Test
    @WithMockUser(authorities = "FILTER_APPLY_PRIVILEGE")
    public void applyFilter_UnsupportedFilter() throws Exception {
        var filters = new String[]{"UNSUPPORTED_FILTER"};

        mockMvc.perform(MockMvcRequestBuilders.post("/image/imageId/filters/apply")
                        .param("filters", filters)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "FILTER_APPLY_PRIVILEGE")
    public void applyFilter_NoFilters() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/image/imageId/filters/apply")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "FILTER_APPLY_PRIVILEGE")
    public void applyFilter_NotFound() throws Exception {
        var filters = new String[]{FilterType.GRAY.name()};

        Mockito.when(filterRequestService.createRequest(Mockito.any(), Mockito.anyList()))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/image/imageId/filters/apply")
                        .param("filters", filters)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void applyFilter_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/image/imageId/filters/apply")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "FILTER_READ_PRIVILEGE")
    public void getModifiedImage_Success() throws Exception {
        var response = new GetModifiedImageByRequestIdResponse("imageId", ImageStatus.DONE);

        Mockito.when(filterRequestService.getRequest(Mockito.any(), Mockito.any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/image/imageId/filters/requestId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageId").value("imageId"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(ImageStatus.DONE.name()));
    }

    @Test
    @WithMockUser(authorities = "FILTER_READ_PRIVILEGE")
    public void getModifiedImage_NotFound() throws Exception {
        Mockito.when(filterRequestService.getRequest(Mockito.any(), Mockito.any()))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/image/imageId/filters/requestId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void getModifiedImage_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/image/imageId/filters/requestId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

}
