package com.market.controller;

import com.market.dto.LoginRequest;
import com.market.dto.LoginResponse;
import com.market.dto.RegisterRequest;
import com.market.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Registration failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Invalid phone number or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Since we're using stateless JWT tokens, logout is handled client-side
        // by removing the token from client storage
        return ResponseEntity.ok("Logged out successfully");
    }
}
