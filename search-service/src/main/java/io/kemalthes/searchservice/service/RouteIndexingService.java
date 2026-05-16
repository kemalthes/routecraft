package io.kemalthes.searchservice.service;

import io.kemalthes.searchservice.dto.PublishedRouteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteIndexingService {

    private static final String PUBLISHED_STATUS = "PUBLISHED";

    private final VectorStore vectorStore;

    public void indexPublishedRoute(PublishedRouteEvent event) {
        vectorStore.delete(List.of(event.routeId().toString()));
        vectorStore.add(List.of(toDocument(event)));
        log.info("Indexed published route {} in search service", event.routeId());
    }

    private Document toDocument(PublishedRouteEvent event) {
        return Document.builder()
                .id(event.routeId().toString())
                .text(event.searchableText())
                .metadata(Map.of(
                        "id", event.routeId().toString(),
                        "version", event.version() == null ? 0L : event.version(),
                        "title", nullToEmpty(event.title()),
                        "description", nullToEmpty(event.description()),
                        "imageObjectName", nullToEmpty(event.imageObjectName()),
                        "distance", event.distance() == null ? 0.0 : event.distance(),
                        "durationMinutes", event.durationMinutes() == null ? 0 : event.durationMinutes(),
                        "authorName", nullToEmpty(event.authorName()),
                        "status", PUBLISHED_STATUS
                ))
                .build();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
