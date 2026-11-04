package com.example.ccmps.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "fraud-service", contextId = "fraudMetrics")
public interface FraudMetricsClient {
    @GetMapping("/api/admin/metrics/fraud")
    long count();
}
