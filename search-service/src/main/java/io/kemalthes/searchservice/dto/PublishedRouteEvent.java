package io.kemalthes.searchservice.dto;

import java.util.UUID;

public record PublishedRouteEvent(
        UUID routeId,
        Long version,
        String title,
        String description,
        String imageObjectName,
        Double distance,
        Integer durationMinutes,
        String authorName
) {
    public String searchableText() {
        return String.join("\n",
                nullToEmpty(title),
                nullToEmpty(description),
                "Distance km: %s".formatted(nullToEmpty(distance)),
                "Duration minutes: %s".formatted(nullToEmpty(durationMinutes)),
                "Author: %s".formatted(nullToEmpty(authorName))
        );
    }

    private static String nullToEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}
