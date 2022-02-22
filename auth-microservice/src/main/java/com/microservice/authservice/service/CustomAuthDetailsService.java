package com.microservice.authservice.service;

import com.microservice.authservice.model.Role;
import com.microservice.authservice.model.UserModel;
import com.microservice.authservice.repository.UserRepository;
import com.microservice.authservice.requests.AuthRequest;
import com.microservice.authservice.response.Response;
import com.microservice.authservice.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomAuthDetailsService implements UserDetailsService {
   @Autowired
   private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private  JwtTokenService jwtTokenService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private final Response response =new Response();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (Boolean.FALSE.equals(userRepository.existsByUsername(username))){
            throw  new UsernameNotFoundException("Username Not Found");
        }
        UserModel userModel = userRepository.findUserModelsByUsername(username);
        return  new CustomUserDetails(userModel);
    }

    public ResponseEntity<Response> registerUser(AuthRequest authRequest){
        String password=bCryptPasswordEncoder.encode(authRequest.getPassword());
        if (userRepository.existsByUsername(authRequest.getUsername())){
            response.setMessage("UserName Already Exists");
            response.setError(true);
            response.setTimestamp(LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(response);
        }
        UserModel userModel=new UserModel();
        userModel.setUsername(authRequest.getUsername());
        userModel.setPassword(password);
        userModel.setRole(Role.ROLE_USER);
        userModel.setCreatedAt(LocalDateTime.now().toString());
        userModel.setUpdatedAt(LocalDateTime.now().toString());
        try {
            userRepository.save(userModel);
        }
        catch (Exception e){
            response.setMessage(e.toString());
            response.setError(true);
            response.setTimestamp(LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(response);
        }
        response.setMessage("You Have Been Registered Successfully");
        response.setError(false);
        response.setTimestamp(LocalDateTime.now().toString());
        return ResponseEntity.ok().body(response);


    }
    public ResponseEntity<Response> signIn(AuthRequest authRequest){

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(),authRequest.getPassword()));
        }
        catch (Exception e){
            response.setMessage(e.getLocalizedMessage());
            response.setError(true);
            response.setTimestamp(LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(response);
        }
        Map<String,Object> payload = new HashMap<>();
        payload.put("username",authRequest.getUsername());
        response.setMessage("Bearer "+jwtTokenService.createJwtToken(payload));
        response.setError(false);
        response.setTimestamp(LocalDateTime.now().toString());
        return ResponseEntity.ok().body(response);

    }
    public ResponseEntity<Response>validateToken(String token){
        if (!jwtTokenService.isTokenValid(token)){
            response.setMessage("Invalid Token");
            response.setError(true);
            response.setTimestamp(LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(response);
        }
        if (jwtTokenService.IsTokenExpired(token)){
            response.setMessage("Expired Token");
            response.setError(true);
            response.setTimestamp(LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(response);
        }

        response.setMessage("Valid Token");
        response.setError(false);
        response.setTimestamp(LocalDateTime.now().toString());
        return ResponseEntity.ok().body(response);
    }

    public String extractUsername(String token){
        return jwtTokenService.ExtractPayload(token);
    }


}
