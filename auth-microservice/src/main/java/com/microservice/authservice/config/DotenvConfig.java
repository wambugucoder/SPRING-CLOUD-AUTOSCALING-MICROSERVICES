package com.microservice.authservice.config;


import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {

    public DotenvBuilder dotenv(){
        return Dotenv.configure()
                .directory("/src/main/resources")
                .filename(".env");
    }
}
