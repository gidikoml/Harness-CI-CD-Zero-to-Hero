package com.example.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Application is healthy");
        return response;
    }

    @GetMapping("/ready")
    public Map<String, String> ready() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "READY");
        response.put("message", "Application is ready to serve traffic");
        return response;
    }
}
