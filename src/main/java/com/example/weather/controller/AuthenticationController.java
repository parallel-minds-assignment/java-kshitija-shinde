package com.example.weather.controller;

import com.example.weather.utils.JWTUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {
    private JWTUtil jwtUtil = new JWTUtil();

    private final String USERNAME = "admin";
    private final String PASSWORD = "password123"; // In real applications, store this securely

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (USERNAME.equals(username) && PASSWORD.equals(password)) {
            String token = JWTUtil.generateToken(username);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
