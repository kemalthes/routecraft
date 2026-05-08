package io.kemalthes.semesterwork3.apigateway.filter;

import io.kemalthes.semesterwork3.apigateway.dto.AuthFilterConfig;
import io.kemalthes.semesterwork3.apigateway.dto.AuthResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import java.time.Duration;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilterConfig> {

    @Value("${auth.validate-uri}")
    private String validateUri;

    private final WebClient webClient;

    public AuthFilter(WebClient webClient) {
        super(AuthFilterConfig.class);
        this.webClient = webClient;
    }

    @Override
    public GatewayFilter apply(AuthFilterConfig config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);
            return webClient.get()
                    .uri(validateUri)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .bodyToMono(AuthResponse.class)
                    .timeout(Duration.ofSeconds(3))
                    .flatMap(authResponse -> {
                        ServerHttpRequest request = exchange.getRequest()
                                .mutate()
                                .header("X-User-Id", String.valueOf(authResponse.userId()))
                                .header("X-User-Role", authResponse.role())
                                .build();
                        ServerWebExchange newExchange = exchange.mutate()
                                .request(request)
                                .build();
                        return chain.filter(newExchange);
                    })
                    .onErrorResume(error -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }
}
