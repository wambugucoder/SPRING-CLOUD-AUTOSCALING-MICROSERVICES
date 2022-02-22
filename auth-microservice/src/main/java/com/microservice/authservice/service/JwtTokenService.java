package com.microservice.authservice.service;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JwtTokenService {


    @Value("${user.name}")
    public String name;


    private String securityKey = Dotenv.load().get("MY_SECRET_KEY");

    @PostConstruct
    protected void init() {
        securityKey = Base64.getEncoder().encodeToString(securityKey.getBytes());
    }

    public String createJwtToken(Map<String, Object> payload) {
        return Jwts.builder()
                .setClaims(payload)
                .setSubject("Temporary Token Issuance")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                .setIssuer(name)
                .signWith(SignatureAlgorithm.HS256, securityKey)
                .compact();


    }
    private Claims extractAllClaims(String token){
        return  Jwts.parser().setSigningKey(securityKey).parseClaimsJws(token).getBody();

    }
    private <R> R extractSpecificClaim(String token, Function<Claims,R> claimsResolver){
        Claims allClaims= extractAllClaims(token);
        return claimsResolver.apply(allClaims);

    }
    private Date expiryDate(String token){
        return extractSpecificClaim(token,Claims::getExpiration);
    }

    public String ExtractPayload(String token){
        Claims allClaims= extractAllClaims(token);
        return (String) allClaims.get("username");
    }

    public boolean IsTokenExpired(String token){
        return expiryDate(token).before(new Date());
    }

    public boolean isTokenValid(String token){
        try {
            Jwts.parser().setSigningKey(securityKey).parseClaimsJws(token);
        }
        catch (JwtException ex) {
            return false;
        }
        return true;
    }
}