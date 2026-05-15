package io.kemalthes.semesterwork3.controller;

import io.kemalthes.semesterwork3.dto.MinioWebhookEvent;
import io.kemalthes.semesterwork3.service.MinioWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks/minio")
@RequiredArgsConstructor
public class MinioWebhookController {

    private final MinioWebhookService minioWebhookService;

    @PostMapping
    public ResponseEntity<Void> handleMinioEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MinioWebhookEvent event) {
        return minioWebhookService.handleWebHook(authHeader, event);
    }
}