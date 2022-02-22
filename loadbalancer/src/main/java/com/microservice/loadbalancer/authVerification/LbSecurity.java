package com.microservice.loadbalancer.authVerification;


import com.microservice.loadbalancer.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Configuration
public class LbSecurity implements GlobalFilter {
    @Autowired
    private RouterValidator routerValidator;
    @Autowired
    private RestTemplate restTemplate;

    private String authUrl="http://AUTHSERVICE/api/auth/";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if(routerValidator.isSecured.test(request)) {
            if (this.isAuthMissing(request))
                return this.onError(exchange, "Authorization header is missing in request");

            final String token = this.getAuthHeader(request);

            if (Boolean.TRUE.equals(isTokenInvalid(token)))
                return this.onError(exchange, "Authorization header is invalid");
        }
        return chain.filter(exchange);

    }
    private Mono<Void> onError(ServerWebExchange exchange, String err) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Type", "application/json");
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Flux.just(buffer));
    }
    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }
    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(7);
    }
    private Boolean isTokenInvalid(String token){
        String url= authUrl + "validate" + "/" + token;
        Response validateResponse= restTemplate.postForObject(url,null, Response.class);
        assert validateResponse != null;
        return  validateResponse.isError();
    }

}
