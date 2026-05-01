package io.kemalthes.semesterwork3.controller;

import io.kemalthes.semesterwork3.config.props.MinioProperties;
import io.kemalthes.semesterwork3.dto.MinioWebhookEvent;
import io.kemalthes.semesterwork3.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks/minio")
@RequiredArgsConstructor
public class MinioWebhookController {

    private final MinioProperties minioProperties;

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<Void> handleMinioEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MinioWebhookEvent event) {
        String expectedHeader = "Bearer %s".formatted(minioProperties.webhookToken());
        if (authHeader == null || !authHeader.equals(expectedHeader)) {
            log.warn("Unauthorized webhook access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (event.eventName() != null && event.eventName().startsWith("s3:ObjectCreated")) {
            String objectKey = event.records().getFirst().s3().object().key();
            log.info("Received MinIO webhook for successfully uploaded object: {}", objectKey);
            routeService.setStatusPending(objectKey);
        }
        return ResponseEntity.ok().build();
    }
}