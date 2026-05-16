package io.kemalthes.searchservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kemalthes.searchservice.config.RabbitConfig;
import io.kemalthes.searchservice.dto.PublishedRouteEvent;
import io.kemalthes.searchservice.service.RouteIndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublishedRouteListener {

    private final RouteIndexingService routeIndexingService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.SEARCH_INDEXING_QUEUE)
    public void handlePublishedRoute(Message message) throws IOException {
        PublishedRouteEvent event = objectMapper.readValue(message.getBody(), PublishedRouteEvent.class);
        log.info("Received route publication event for route {}", event.routeId());
        routeIndexingService.indexPublishedRoute(event);
    }
}
