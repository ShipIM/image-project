package com.example.imageproject.service;

import com.example.imageproject.config.BaseTest;
import com.example.imageproject.dto.rest.image.GetModifiedImageByRequestIdResponse;
import com.example.imageproject.exception.EntityNotFoundException;
import com.example.imageproject.exception.IllegalAccessException;
import com.example.imageproject.model.entity.FilterRequest;
import com.example.imageproject.model.entity.Image;
import com.example.imageproject.model.entity.User;
import com.example.imageproject.model.enumeration.ImageStatus;
import com.example.imageproject.model.enumeration.RoleEnum;
import com.example.imageproject.repository.FilterRequestRepository;
import com.example.imageproject.repository.ImageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.stream.Stream;

@Transactional
public class FilterRequestServiceTest extends BaseTest {

    @Autowired
    private FilterRequestService filterRequestService;
    @Autowired
    private FilterRequestRepository filterRequestRepository;
    @Autowired
    private ImageRepository imageRepository;

    @Test
    public void getFilterRequestByRequestId_RequestExists() {
        var imageId = "originalId";
        var image = new Image(null, "filename.jpeg", 1L, imageId, 1L);
        imageRepository.save(image);

        var requestId = "requestId";
        var filterRequest = new FilterRequest(null, ImageStatus.WIP, imageId, null,
                requestId, 1L);
        var result = filterRequestRepository.save(filterRequest);

        Assertions.assertEquals(result, filterRequestService.getFilterRequestByRequestId(requestId));
    }

    @Test
    public void getFilterRequestByRequestId_RequestNotExists() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> filterRequestService
                .getFilterRequestByRequestId("requestId"));
    }


    @Test
    public void getRequest_HasNoAccess() {
        var userId = 1L;
        var authentication = new UsernamePasswordAuthenticationToken(new User(userId + 1, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "originalId";
        var image = new Image(null, "filename", 1L, imageId, userId);
        imageRepository.save(image);

        Assertions.assertThrows(IllegalAccessException.class, () -> filterRequestService
                .getRequest("requestId", imageId));
    }

    @ParameterizedTest
    @MethodSource("getArguments")
    public void getRequest_RequestExists(FilterRequest filterRequest,
                                         GetModifiedImageByRequestIdResponse expected) {
        var authentication = new UsernamePasswordAuthenticationToken(new User(1L, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "originalId";
        var image = new Image(null, "filename.jpeg", 1L, imageId, 1L);
        imageRepository.save(image);

        filterRequestRepository.save(filterRequest);

        Assertions.assertEquals(expected, filterRequestService.getRequest(filterRequest.getRequestId(), imageId));
    }

    private static Stream<Arguments> getArguments() {
        var userId = 1L;
        var imageId = "originalId";

        var filterRequestWip = new FilterRequest(null, ImageStatus.WIP, imageId, null,
                "requestId", userId);
        var expectedResponseWip = new GetModifiedImageByRequestIdResponse(imageId, ImageStatus.WIP);

        var modifiedImageId = "modifiedId";
        var filterRequestDone = new FilterRequest(null, ImageStatus.DONE, imageId, modifiedImageId,
                "requestId", userId);
        var expectedResponseDone = new GetModifiedImageByRequestIdResponse(modifiedImageId, ImageStatus.DONE);

        return Stream.of(
                Arguments.of(filterRequestWip, expectedResponseWip),
                Arguments.of(filterRequestDone, expectedResponseDone)
        );
    }

    @Test
    public void getRequest_RequestNotExists() {
        var authentication = new UsernamePasswordAuthenticationToken(new User(1L, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var image = new Image(null, "filename", 1L, "originalId", 1L);
        imageRepository.save(image);

        Assertions.assertThrows(EntityNotFoundException.class, () -> filterRequestService
                .getRequest("originalId", "requestId"));
    }

}