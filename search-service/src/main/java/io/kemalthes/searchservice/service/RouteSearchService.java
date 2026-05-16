package io.kemalthes.searchservice.service;

import io.kemalthes.search.dto.SearchRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteSearchService {

    private static final int DEFAULT_LIMIT = 12;
    private static final int MAX_LIMIT = 20;

    private final CurrentUserService currentUserService;
    private final VectorStore vectorStore;
    private final SearchMinioService minioService;

    public List<SearchRouteResponse> searchRoutes(String query, Integer limit) {
        if (!currentUserService.hasAdminRole()) {
            throw new AccessDeniedException("Semantic search is available only for admins");
        }
        String normalizedQuery = normalizeQuery(query);
        int normalizedLimit = normalizeLimit(limit);
        SearchRequest request = SearchRequest.builder()
                .query(normalizedQuery)
                .topK(normalizedLimit)
                .filterExpression("status == 'PUBLISHED'")
                .build();
        return vectorStore.similaritySearch(request).stream()
                .map(this::toResponse)
                .toList();
    }

    private SearchRouteResponse toResponse(Document document) {
        var metadata = document.getMetadata();
        String imageObjectName = metadata.getOrDefault("imageObjectName", "").toString();
        return new SearchRouteResponse()
                .id(java.util.UUID.fromString(metadata.getOrDefault("id", "").toString()))
                .version(parseLongSafe(metadata.get("version")))
                .title(metadata.getOrDefault("title", "").toString())
                .description(metadata.getOrDefault("description", "").toString())
                .imageUrl(imageObjectName.isEmpty() ? "" : minioService.getPresignedUrl(imageObjectName))
                .distance(parseDoubleSafe(metadata.get("distance")))
                .durationMinutes(parseIntSafe(metadata.get("durationMinutes")))
                .authorName(metadata.getOrDefault("authorName", "").toString())
                .status(SearchRouteResponse.StatusEnum.PUBLISHED)
                .isLiked(false)
                .score(document.getScore() == null ? 0.0f : document.getScore().floatValue());
    }

    private Long parseLongSafe(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return (long) Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    private Double parseDoubleSafe(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private Integer parseIntSafe(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return (int) Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private String normalizeQuery(String query) {
        if (!StringUtils.hasText(query) || query.trim().length() < 2) {
            throw new IllegalArgumentException("Search query must contain at least 2 characters");
        }
        return query.trim();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Search limit must be between 1 and 20");
        }
        return limit;
    }
}