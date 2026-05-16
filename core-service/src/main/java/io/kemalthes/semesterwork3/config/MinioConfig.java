package io.kemalthes.semesterwork3.config;

import io.kemalthes.semesterwork3.config.props.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@RequiredArgsConstructor
@Configuration
public class MinioConfig {

    private static final String DEFAULT_REGION = "us-east-1";

    private final MinioProperties minioProperties;

    @Bean
    @Primary
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.endpoint())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();
    }

    @Bean
    public MinioClient presignedMinioClient() {
        String endpoint = minioProperties.publicEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = minioProperties.endpoint();
        }
        return MinioClient.builder()
                .endpoint(endpoint)
                .region(DEFAULT_REGION)
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();
    }
}
