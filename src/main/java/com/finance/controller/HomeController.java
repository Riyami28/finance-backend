package com.finance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "app", "Finance Dashboard Backend",
            "version", "1.0.0",
            "status", "running",
            "endpoints", Map.of(
                "auth", "/api/auth/login (POST), /api/auth/register (POST)",
                "records", "/api/records (GET, POST, PUT, DELETE)",
                "dashboard", "/api/dashboard/summary (GET)",
                "users", "/api/users (GET, POST, PATCH)"
            )
        );
    }
}
