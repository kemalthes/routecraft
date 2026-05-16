package io.kemalthes.searchservice.service;

import io.kemalthes.searchservice.config.props.MinioProperties;
import io.kemalthes.searchservice.exception.SearchServiceException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
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
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(properties.bucket())
                            .object(objectName)
                            .method(Http.Method.GET)
                            .expiry(properties.presignedUrlTtl(), TimeUnit.MINUTES)
                            .build()
            );
            return toPublicMinioUrl(url);
        } catch (Exception e) {
            throw new SearchServiceException("Cannot create route image URL", HttpStatus.SERVICE_UNAVAILABLE, e);
        }
    }

    private String toPublicMinioUrl(String url) throws URISyntaxException {
        String publicEndpoint = properties.publicEndpoint();
        if (publicEndpoint == null || publicEndpoint.isBlank()) {
            return url;
        }
        URI source = URI.create(url);
        URI target = URI.create(publicEndpoint);
        return new URI(
                target.getScheme(),
                target.getUserInfo(),
                target.getHost(),
                target.getPort(),
                source.getPath(),
                source.getQuery(),
                source.getFragment()
        ).toString();
    }
}
