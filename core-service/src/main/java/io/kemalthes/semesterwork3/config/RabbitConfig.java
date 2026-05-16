package io.kemalthes.semesterwork3.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String SEARCH_INDEXING_EXCHANGE = "route.search.exchange";
    public static final String SEARCH_INDEXING_QUEUE = "route.search.published";
    public static final String SEARCH_INDEXING_ROUTING_KEY = "route.published";

    @Bean
    public DirectExchange routeSearchExchange() {
        return new DirectExchange(SEARCH_INDEXING_EXCHANGE, true, false);
    }

    @Bean
    public Queue routeSearchQueue() {
        return QueueBuilder.durable(SEARCH_INDEXING_QUEUE).build();
    }

    @Bean
    public Binding routeSearchBinding(
            Queue routeSearchQueue,
            DirectExchange routeSearchExchange
    ) {
        return BindingBuilder.bind(routeSearchQueue)
                .to(routeSearchExchange)
                .with(SEARCH_INDEXING_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }
}
