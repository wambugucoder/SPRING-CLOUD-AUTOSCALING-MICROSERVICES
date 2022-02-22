package com.microservice.authservice.requests;

import lombok.Getter;

@Getter
public class AuthRequest {
    private String username;
    private String password;
}
