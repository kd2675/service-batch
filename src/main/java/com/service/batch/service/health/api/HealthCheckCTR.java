package com.service.batch.service.health.api;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
public class HealthCheckCTR {

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
