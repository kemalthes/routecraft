package io.kemalthes.semesterwork3.service;

import io.kemalthes.semesterwork3.config.CacheNames;
import io.kemalthes.semesterwork3.config.props.MinioProperties;
import io.kemalthes.semesterwork3.exception.InternalMinioException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String putPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(objectName)
                            .method(Http.Method.PUT)
                            .expiry(minioProperties.presignedUrlTtl(), TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new InternalMinioException("Cannot get presigned url");
        }
    }

    @Cacheable(cacheNames = CacheNames.MINIO_GET_PRESIGNED_URLS, key = "#objectName")
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(objectName)
                            .method(Http.Method.GET)
                            .expiry(minioProperties.presignedUrlTtl(), TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new InternalMinioException("Cannot get presigned url");
        }
    }

    public String generateObjectName(String fileName) {
        String safeFileName = fileName == null || fileName.isBlank()
                ? "route-preview"
                : fileName.trim().replaceAll("[^a-zA-Z0-9._-]", "-");
        return "previews/%s-%s.jpg".formatted(safeFileName, UUID.randomUUID());
    }
}
