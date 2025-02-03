package com.microcore.springcloud.msvc.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class SampleGlobalFilter implements GlobalFilter, Ordered {

    private final Logger logger = LoggerFactory.getLogger(SampleGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Filter executing before to the request PRE");

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate().header("token", "ABCDEFG").build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            logger.info("Filter executing after to the request POST");
            String token = mutatedExchange.getRequest().getHeaders().getFirst("token");
            if (token != null) {
                logger.info("Token: " + token);
                mutatedExchange.getResponse().getHeaders().add("token", token);
            }

            Optional.ofNullable(mutatedExchange.getRequest().getHeaders().getFirst("token")).ifPresent(value -> {
                logger.info("Token in the response: " + value);
                mutatedExchange.getResponse().getHeaders().add("token", value);
            });

            mutatedExchange.getResponse().getCookies().add("color", ResponseCookie.from("color", "red").build());
            mutatedExchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        }));
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
