package com.microservice.authservice.controller;

import com.microservice.authservice.requests.AuthRequest;
import com.microservice.authservice.response.Response;
import com.microservice.authservice.service.CustomAuthDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    @Autowired
    private CustomAuthDetailsService customAuthDetailsService;

    @Value("${eureka.instance.metadataMap.zone}")
    private String zone;

    @GetMapping(value = "/zone", produces = MediaType.APPLICATION_JSON_VALUE)
    public String zone() {
        return "{\"zone\"=\"" + zone + "\"}";
    }


    @PostMapping(value = "/signup",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> registerUser(@RequestBody AuthRequest authRequest){
        return  customAuthDetailsService.registerUser(authRequest);
    }
    @PostMapping(value = "/signin",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> loginUser(@RequestBody AuthRequest authRequest){
        return  customAuthDetailsService.signIn(authRequest);
    }
    @PostMapping(value = "/validate/{token}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> validateToken(@PathVariable String token){
        return  customAuthDetailsService.validateToken(token);
    }
}
