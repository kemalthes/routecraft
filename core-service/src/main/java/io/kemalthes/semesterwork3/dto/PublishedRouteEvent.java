package io.kemalthes.semesterwork3.dto;

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
}
