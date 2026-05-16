package io.kemalthes.searchservice.config;

import io.kemalthes.searchservice.config.props.MinioProperties;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MinioConfig {

    private static final String DEFAULT_REGION = "us-east-1";

    @Bean
    @Primary
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }

    @Bean
    public MinioClient presignedMinioClient(MinioProperties properties) {
        String endpoint = properties.publicEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = properties.endpoint();
        }
        return MinioClient.builder()
                .endpoint(endpoint)
                .region(DEFAULT_REGION)
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }
}
