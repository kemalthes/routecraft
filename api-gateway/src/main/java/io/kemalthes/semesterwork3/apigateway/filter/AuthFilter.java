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

import java.net.InetSocketAddress;
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
            ServerWebExchange sanitizedExchange = stripUserHeaders(exchange);
            if (!sanitizedExchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                if (!config.isRequired()) {
                    return chain.filter(sanitizedExchange);
                }
                sanitizedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return sanitizedExchange.getResponse().setComplete();
            }
            String authHeader = sanitizedExchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);
            String clientIp = resolveClientIp(sanitizedExchange.getRequest());
            return webClient.get()
                    .uri(validateUri)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .header("X-Forwarded-For", clientIp)
                    .retrieve()
                    .bodyToMono(AuthResponse.class)
                    .timeout(Duration.ofSeconds(3))
                    .flatMap(authResponse -> {
                        ServerHttpRequest request = sanitizedExchange.getRequest()
                                .mutate()
                                .header("X-User-Id", String.valueOf(authResponse.userId()))
                                .header("X-User-Role", authResponse.role())
                                .build();
                        ServerWebExchange newExchange = sanitizedExchange.mutate()
                                .request(request)
                                .build();
                        return chain.filter(newExchange);
                    })
                    .onErrorResume(error -> {
                        if (!config.isRequired()) {
                            return chain.filter(sanitizedExchange);
                        }
                        sanitizedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return sanitizedExchange.getResponse().setComplete();
                    });
        };
    }

    private ServerWebExchange stripUserHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-User-Role");
                })
                .build();
        return exchange.mutate()
                .request(request)
                .build();
    }

    private String resolveClientIp(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return "unknown";
        }
        return remoteAddress.getAddress().getHostAddress();
    }
}
