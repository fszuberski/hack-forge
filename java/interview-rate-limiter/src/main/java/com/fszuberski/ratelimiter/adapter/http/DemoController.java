package com.fszuberski.ratelimiter.adapter.http;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/api/greeting")
    public Map<String, String> greeting() {
        return Map.of("message", "Hello, candidate!");
    }

    @GetMapping("/api/items")
    public List<String> items() {
        return List.of("book", "pen");
    }

    @PostMapping("/api/echo")
    public Map<String, String> echo(@RequestBody Map<String, String> body) {
        return body;
    }
}
