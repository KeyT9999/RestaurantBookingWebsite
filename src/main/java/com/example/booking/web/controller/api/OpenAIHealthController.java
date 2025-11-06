package com.example.booking.web.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.service.ai.OpenAIService;

/**
 * Health-check endpoints for OpenAI integration
 */
@RestController
@RequestMapping("/api/openai")
public class OpenAIHealthController {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIHealthController.class);

    @Autowired
    private OpenAIService openAIService;

    /**
     * Check if OpenAI API key and connectivity are working
     */
    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_OWNER','CUSTOMER')")
    public ResponseEntity<Map<String, Object>> check(Authentication authentication) {
        long start = System.nanoTime();
        Map<String, Object> body = new HashMap<>();
        try {
            String result = openAIService.ping().join();
            long latency = (System.nanoTime() - start) / 1_000_000;
            body.put("ok", true);
            body.put("result", result);
            body.put("latencyMs", latency);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            long latency = (System.nanoTime() - start) / 1_000_000;
            logger.warn("OpenAI check failed: {}", e.getMessage());
            body.put("ok", false);
            body.put("error", e.getMessage());
            body.put("latencyMs", latency);
            // If key invalid, still return 200 with ok=false for easy diagnostics
            return ResponseEntity.ok(body);
        }
    }

    /**
     * Try a sample rewrite with optional input text
     */
    @PostMapping("/improve-sample")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_OWNER','CUSTOMER')")
    public ResponseEntity<Map<String, Object>> improveSample(@RequestBody(required = false) Map<String, String> req) {
        String input = req != null && req.get("text") != null && !req.get("text").trim().isEmpty()
            ? req.get("text").trim()
            : "Quán khá ổn, món nướng thơm, phục vụ nhanh.";

        long start = System.nanoTime();
        Map<String, Object> body = new HashMap<>();
        try {
            String improved = openAIService.improveText(input, "debug", "Test rewrite").join();
            long latency = (System.nanoTime() - start) / 1_000_000;
            body.put("ok", true);
            body.put("input", input);
            body.put("improvedText", improved);
            body.put("latencyMs", latency);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            long latency = (System.nanoTime() - start) / 1_000_000;
            logger.warn("Improve sample failed: {}", e.getMessage());
            body.put("ok", false);
            body.put("error", e.getMessage());
            body.put("latencyMs", latency);
            return ResponseEntity.ok(body);
        }
    }
}


