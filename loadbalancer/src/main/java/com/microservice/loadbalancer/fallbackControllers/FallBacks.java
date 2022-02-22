package com.microservice.loadbalancer.fallbackControllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import response.FallBackResponse;

import java.time.LocalDateTime;

@RestController
public class FallBacks {

    @GetMapping("/auth-fallback")
    public Mono<ResponseEntity<FallBackResponse>> getAuthFallBack(){
        FallBackResponse fallBackResponse =new FallBackResponse();
        fallBackResponse.setMessage("The Authentication Microservice is Not Available At the moment");
        fallBackResponse.setTimestamp(LocalDateTime.now().toString());
        return Mono.just(ResponseEntity.badRequest().body(fallBackResponse));
    }



}
