package io.kemalthes.semesterwork3.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties (
        String endpoint,
        String accessKey,
        String secretKey,
        String bucket,
        int presignedUrlTtl,
        String webhookToken) {
}
