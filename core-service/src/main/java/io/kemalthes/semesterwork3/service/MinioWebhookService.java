package io.kemalthes.semesterwork3.service;

import io.kemalthes.semesterwork3.config.props.MinioProperties;
import io.kemalthes.semesterwork3.dto.MinioWebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioWebhookService {

    private final MinioProperties minioProperties;
    private final RouteService routeService;

    @Transactional
    public ResponseEntity<Void> handleWebHook(String authHeader, MinioWebhookEvent event) {
        String expectedHeader = "Bearer %s".formatted(minioProperties.webhookToken());
        if (authHeader == null || !authHeader.equals(expectedHeader)) {
            log.warn("Unauthorized webhook access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (event.eventName() != null && event.eventName().startsWith("s3:ObjectCreated")) {
            String objectKey = extractObjectKey(event);
            if (objectKey == null || objectKey.isBlank()) {
                log.warn("MinIO webhook object key is missing");
                return ResponseEntity.ok().build();
            }
            String routeObjectKey = removeBucketPrefix(objectKey);
            log.info("Received MinIO webhook for successfully uploaded object: {}", routeObjectKey);
            routeService.setStatusPending(routeObjectKey);
        }
        return ResponseEntity.ok().build();
    }

    private String extractObjectKey(MinioWebhookEvent event) {
        String objectKey = event.key();
        if ((objectKey == null || objectKey.isBlank())
                && event.records() != null
                && !event.records().isEmpty()
                && event.records().getFirst().s3() != null
                && event.records().getFirst().s3().object() != null) {
            objectKey = event.records().getFirst().s3().object().key();
        }
        return objectKey == null ? null : URLDecoder.decode(objectKey, StandardCharsets.UTF_8);
    }

    private String removeBucketPrefix(String objectKey) {
        String bucketPrefix = minioProperties.bucket() + "/";
        if (objectKey.startsWith(bucketPrefix)) {
            return objectKey.substring(bucketPrefix.length());
        }
        return objectKey;
    }
}
