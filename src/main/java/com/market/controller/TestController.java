package com.market.controller;

import com.market.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> testAuth() {
        try {
            authenticationService.requireAuthentication();
            
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("userId", authenticationService.getCurrentUserId());
            response.put("message", "Authentication successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(401).body(response);
        }
    }
    
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> testPublic() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/error")
    public ResponseEntity<Map<String, String>> testError() {
        throw new RuntimeException("This is a test error to verify error handling");
    }
}
