package io.kemalthes.semesterwork3.service;

import io.kemalthes.semesterwork3.config.RabbitConfig;
import io.kemalthes.semesterwork3.dto.PublishedRouteEvent;
import io.kemalthes.semesterwork3.entity.TourRoute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteSearchPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAfterCommit(TourRoute route) {
        PublishedRouteEvent event = toEvent(route);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(event);
                }
            });
            return;
        }
        publish(event);
    }

    private void publish(PublishedRouteEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.SEARCH_INDEXING_EXCHANGE,
                    RabbitConfig.SEARCH_INDEXING_ROUTING_KEY,
                    event);
            log.info("Published route {} to search indexing exchange", event.routeId());
        } catch (RuntimeException e) {
            log.error("Cannot publish route {} to search indexing exchange", event.routeId(), e);
        }
    }

    private PublishedRouteEvent toEvent(TourRoute route) {
        return new PublishedRouteEvent(
                route.getId(),
                route.getVersion(),
                route.getTitle(),
                route.getDescription(),
                route.getImageUrl(),
                route.getDistance() == null ? null : route.getDistance().doubleValue(),
                route.getDurationMinutes(),
                route.getAuthor() == null ? null : route.getAuthor().getUsername()
        );
    }
}
