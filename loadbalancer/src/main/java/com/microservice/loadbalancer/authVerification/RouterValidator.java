package com.microservice.loadbalancer.authVerification;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    private static final List<String> openApiEndpoints= List.of(
            "/api/auth/signup",
            "/api/auth/signin",
            "/api/auth/zone"
    );

     public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}

