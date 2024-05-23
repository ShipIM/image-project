package com.example.filter.imagefilter;

import com.example.filter.config.BaseTest;
import com.example.filter.exception.ConversionFailedException;
import com.example.filter.exception.RetryableException;
import com.example.filter.model.enumeration.FilterType;
import io.github.bucket4j.Bucket;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.Function;

public class RecognitionFilterTest extends BaseTest {

    @Autowired
    private Retry retry;
    @Autowired
    private CircuitBreaker circuitBreaker;

    private final RestClient restClient = Mockito.mock(RestClient.class);
    private final Bucket bucket = Mockito.mock(Bucket.class);

    private RecognitionFilter recognitionFilter;

    private static byte[] originalImageBytes;

    @PostConstruct
    private void initFilter() {
        recognitionFilter = new RecognitionFilter(restClient, retry, circuitBreaker, bucket);
    }

    @BeforeAll
    private static void init() throws Exception {
        var width = 100;
        var height = 100;
        var originalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        var colorOutputStream = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "png", colorOutputStream);
        originalImageBytes = colorOutputStream.toByteArray();
    }

    @Test
    public void convert_Success() {
        Mockito.when(bucket.tryConsume(Mockito.anyLong())).thenReturn(true);

        var uploadId = "uploadId";
        var tags = List.of(
                new RecognitionFilter.TagsResponse.Tag(100.,
                        new RecognitionFilter.TagsResponse.TagDetail("tag1")),
                new RecognitionFilter.TagsResponse.Tag(90.,
                        new RecognitionFilter.TagsResponse.TagDetail("tag2")),
                new RecognitionFilter.TagsResponse.Tag(80.,
                        new RecognitionFilter.TagsResponse.TagDetail("tag3"))
                );
        var successStatus = new RecognitionFilter.ResponseStatus("success", "");

        var requestHeaderMock = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
        var requestBodyMock = Mockito.mock(RestClient.RequestBodyUriSpec.class);
        var postResponseMock = Mockito.mock(RestClient.ResponseSpec.class);
        var getResponseMock = Mockito.mock(RestClient.ResponseSpec.class);

        Mockito.when(restClient.post()).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.uri(Mockito.anyString())).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.contentType(Mockito.any())).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.body(Mockito.any(Object.class))).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.retrieve()).thenReturn(postResponseMock);
        Mockito.when(postResponseMock.onStatus(Mockito.any(), Mockito.any())).thenReturn(postResponseMock);
        Mockito.when(postResponseMock.body(Mockito.any(Class.class)))
                .thenReturn(new RecognitionFilter.UploadsResponse(
                        new RecognitionFilter.UploadsResponse.UploadsResult(uploadId),
                        successStatus
                ));

        Mockito.when(restClient.get()).thenReturn(requestHeaderMock);
        Mockito.when(requestHeaderMock.uri(Mockito.any(Function.class))).thenReturn(requestHeaderMock);
        Mockito.when(requestHeaderMock.retrieve()).thenReturn(getResponseMock);
        Mockito.when(getResponseMock.onStatus(Mockito.any(), Mockito.any())).thenReturn(getResponseMock);
        Mockito.when(getResponseMock.body(Mockito.any(Class.class)))
                .thenReturn(new RecognitionFilter.TagsResponse(
                        new RecognitionFilter.TagsResponse.TagsResult(tags),
                        successStatus
                ));

        Assertions.assertDoesNotThrow(() -> recognitionFilter.convert(originalImageBytes));

        Mockito.verify(restClient, Mockito.times(1)).post();
        Mockito.verify(restClient, Mockito.times(1)).get();
    }

    @Test
    public void convert_RetryFailedOnPostRequest() {
        Mockito.when(bucket.tryConsume(Mockito.anyLong())).thenReturn(true);

        var requestBodyMock = Mockito.mock(RestClient.RequestBodyUriSpec.class);
        var postResponseMock = Mockito.mock(RestClient.ResponseSpec.class);

        Mockito.when(restClient.post()).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.uri(Mockito.anyString())).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.contentType(Mockito.any())).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.body(Mockito.any(Object.class))).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.retrieve()).thenReturn(postResponseMock);
        Mockito.when(postResponseMock.onStatus(Mockito.any(), Mockito.any())).thenThrow(RetryableException.class);

        Assertions.assertThrows(ConversionFailedException.class,
                () -> recognitionFilter.convert(originalImageBytes));

        Mockito.verify(restClient, Mockito.times(3)).post();
    }

    @Test
    public void convert_RetryFailedOnGetRequest() {
        Mockito.when(bucket.tryConsume(Mockito.anyLong())).thenReturn(true);

        var uploadId = "uploadId";
        var successStatus = new RecognitionFilter.ResponseStatus("success", "");

        var requestHeaderMock = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
        var requestBodyMock = Mockito.mock(RestClient.RequestBodyUriSpec.class);
        var postResponseMock = Mockito.mock(RestClient.ResponseSpec.class);
        var getResponseMock = Mockito.mock(RestClient.ResponseSpec.class);

        Mockito.when(restClient.post()).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.uri(Mockito.anyString())).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.contentType(Mockito.any())).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.body(Mockito.any(Object.class))).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.retrieve()).thenReturn(postResponseMock);
        Mockito.when(postResponseMock.onStatus(Mockito.any(), Mockito.any())).thenReturn(postResponseMock);
        Mockito.when(postResponseMock.body(Mockito.any(Class.class)))
                .thenReturn(new RecognitionFilter.UploadsResponse(
                        new RecognitionFilter.UploadsResponse.UploadsResult(uploadId),
                        successStatus
                ));

        Mockito.when(restClient.get()).thenReturn(requestHeaderMock);
        Mockito.when(requestHeaderMock.uri(Mockito.any(Function.class))).thenReturn(requestHeaderMock);
        Mockito.when(requestHeaderMock.retrieve()).thenReturn(getResponseMock);
        Mockito.when(getResponseMock.onStatus(Mockito.any(), Mockito.any())).thenThrow(RetryableException.class);

        Assertions.assertThrows(ConversionFailedException.class,
                () -> recognitionFilter.convert(originalImageBytes));

        Mockito.verify(restClient, Mockito.times(3)).get();
    }

    @Test
    public void convert_RateLimiterLimitReached() {
        Mockito.when(bucket.tryConsume(Mockito.anyLong())).thenReturn(false);

        Assertions.assertThrows(ConversionFailedException.class,
                () -> recognitionFilter.convert(originalImageBytes));
    }

    @Test
    public void getFilterType() {
        Assertions.assertEquals(FilterType.RECOGNITION, recognitionFilter.getFilterType());
    }

}
