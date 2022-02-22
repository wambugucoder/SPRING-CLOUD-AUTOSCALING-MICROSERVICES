package com.microservice.loadbalancer.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class Response implements Serializable {
    private  String message;
    private  boolean error;
    private String timestamp;
}
