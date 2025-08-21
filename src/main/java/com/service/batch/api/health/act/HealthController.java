package com.service.batch.api.health.act;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/batch/health")
public class HealthController {
    @RequestMapping
    public String health() {
        return "ok";
    }
}
