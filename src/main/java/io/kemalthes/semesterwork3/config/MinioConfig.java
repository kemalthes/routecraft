package io.kemalthes.semesterwork3.config;

import io.kemalthes.semesterwork3.config.props.MinioProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.minio.MinioClient;

@RequiredArgsConstructor
@Configuration
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.endpoint())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();
    }
}
