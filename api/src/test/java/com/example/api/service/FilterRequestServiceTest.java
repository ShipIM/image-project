package com.example.api.service;

import com.example.api.config.BaseTest;
import com.example.api.dto.kafka.image.ImageDone;
import com.example.api.dto.kafka.image.ImageFilterRequest;
import com.example.api.dto.rest.image.GetModifiedImageByRequestIdResponse;
import com.example.api.exception.EntityNotFoundException;
import com.example.api.exception.IllegalAccessException;
import com.example.api.model.entity.FilterRequest;
import com.example.api.model.entity.Image;
import com.example.api.model.entity.User;
import com.example.api.model.enumeration.FilterType;
import com.example.api.model.enumeration.ImageStatus;
import com.example.api.model.enumeration.RoleEnum;
import com.example.api.repository.FilterRequestRepository;
import com.example.api.repository.ImageRepository;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Transactional
public class FilterRequestServiceTest extends BaseTest {

    @Autowired
    private FilterRequestService filterRequestService;
    @Autowired
    private FilterRequestRepository filterRequestRepository;
    @Autowired
    private ImageRepository imageRepository;
    @MockBean
    private KafkaTemplate<String, ImageFilterRequest> kafkaTemplate;
    @MockBean
    private MinioService minioService;

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

    @Test
    public void getRequest_EntitiesMismatch() {
        var userId = 1L;
        var authentication = new UsernamePasswordAuthenticationToken(new User(userId, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "originalId";
        var image = new Image(null, "filename", 1L, imageId, userId);
        imageRepository.save(image);

        var another = "anotherId";
        var anotherImage = new Image(null, "filename", 1L, another, userId);
        imageRepository.save(anotherImage);

        var filterRequest = new FilterRequest(null, ImageStatus.WIP, another, null,
                "requestId", userId);
        filterRequestRepository.save(filterRequest);

        Assertions.assertThrows(EntityNotFoundException.class, () -> filterRequestService
                .getRequest("requestId", imageId));
    }

    @ParameterizedTest
    @MethodSource("getArguments")
    public void getRequest_RequestExists(FilterRequest filterRequest,
                                         GetModifiedImageByRequestIdResponse expected) {
        var userId = 1L;
        var authentication = new UsernamePasswordAuthenticationToken(new User(userId, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var imageId = "originalId";
        var image = new Image(null, "filename", 1L, imageId, userId);
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

    @Test
    public void createRequest_Success() throws ExecutionException, InterruptedException {
        var userId = 1L;
        var imageId = "originalId";
        var filters = List.of(FilterType.values());

        var authentication = new UsernamePasswordAuthenticationToken(new User(userId, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var image = new Image(null, "filename", 1L, imageId, userId);
        imageRepository.save(image);

        var future = Mockito.mock(CompletableFuture.class);
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any())).thenReturn(future);
        Mockito.when(future.get()).thenReturn(null);

        var result = filterRequestService.createRequest(imageId, filters);

        Mockito.verify(kafkaTemplate, Mockito.times(1)).send(Mockito.any(), Mockito.any());
        Assertions.assertTrue(filterRequestRepository.existsByRequestId(result.getRequestId()));

        var request = filterRequestService.getFilterRequestByRequestId(result.getRequestId());
        Assertions.assertAll(
                () -> Assertions.assertEquals(imageId, request.getOriginalId()),
                () -> Assertions.assertEquals(ImageStatus.WIP, request.getStatus()),
                () -> Assertions.assertEquals(userId, request.getUserId())
        );
    }

    @Test
    public void createRequest_Fail() throws ExecutionException, InterruptedException {
        var authentication = new UsernamePasswordAuthenticationToken(new User(1L, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var image = new Image(null, "filename", 1L, "originalId", 1L);
        imageRepository.save(image);

        var future = Mockito.mock(CompletableFuture.class);
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any())).thenReturn(future);
        Mockito.when(future.get()).thenThrow(new ExecutionException(new KafkaException("Some kafka exception")));

        Assertions.assertThrows(KafkaException.class, () -> filterRequestService
                .createRequest("originalId", Collections.emptyList()));
        Mockito.verify(kafkaTemplate, Mockito.times(3)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void createRequest_ImageNotExists() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> filterRequestService
                .createRequest("originalId", Collections.emptyList()));
    }

    @Test
    public void createRequest_HasNoAccess() {
        var userId = 1L;
        var authentication = new UsernamePasswordAuthenticationToken(new User(userId + 1, "username",
                "password", RoleEnum.ROLE_USER, Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var image = new Image(null, "filename", 1L, "originalId", userId);
        imageRepository.save(image);

        Assertions.assertThrows(IllegalAccessException.class, () -> filterRequestService
                .createRequest("originalId", Collections.emptyList()));
    }

    @Test
    public void consume_Success() {
        var userId = 1L;
        var imageId = "originalId";
        var modifiedImageId = "modifiedId";
        var image = new Image(null, "filename", 1L, imageId, userId);
        imageRepository.save(image);

        var requestId = "requestId";
        var filterRequest = new FilterRequest(null, ImageStatus.WIP, imageId, null,
                requestId, 1L);
        filterRequestRepository.save(filterRequest);

        var acknowledgment = Mockito.mock(Acknowledgment.class);
        Mockito.when(minioService.download(Mockito.any())).thenReturn(new byte[1]);

        filterRequestService.consume(new ImageDone(modifiedImageId, requestId), acknowledgment);

        Mockito.verify(acknowledgment, Mockito.times(1)).acknowledge();

        var request = filterRequestService.getFilterRequestByRequestId(requestId);
        Assertions.assertAll(
                () -> Assertions.assertEquals(ImageStatus.DONE, request.getStatus()),
                () -> Assertions.assertEquals(modifiedImageId, request.getModifiedId())
        );
    }

}