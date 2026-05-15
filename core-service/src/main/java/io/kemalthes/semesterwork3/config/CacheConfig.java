package io.kemalthes.semesterwork3.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kemalthes.semesterwork3.config.props.MinioProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration OSRM_ROUTE_METRICS_TTL = Duration.ofHours(12);

    private final ObjectMapper objectMapper;
    private final MinioProperties minioProperties;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = GenericJackson2JsonRedisSerializer.builder()
                .objectMapper(objectMapper.copy())
                .defaultTyping(true)
                .build();
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(DEFAULT_CACHE_TTL)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                CacheNames.OSRM_ROUTE_METRICS, defaultConfiguration.entryTtl(OSRM_ROUTE_METRICS_TTL),
                CacheNames.MINIO_GET_PRESIGNED_URLS, defaultConfiguration.entryTtl(
                        Duration.ofMinutes(Math.max(1, minioProperties.presignedUrlTtl() - 1L)))
        );
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
