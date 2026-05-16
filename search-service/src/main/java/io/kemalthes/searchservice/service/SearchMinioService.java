package io.kemalthes.searchservice.service;

import io.kemalthes.searchservice.config.props.MinioProperties;
import io.kemalthes.searchservice.exception.SearchServiceException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SearchMinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public String getPresignedUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return "";
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(properties.bucket())
                            .object(objectName)
                            .method(Http.Method.GET)
                            .expiry(properties.presignedUrlTtl(), TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new SearchServiceException("Cannot create route image URL", HttpStatus.SERVICE_UNAVAILABLE, e);
        }
    }
}
