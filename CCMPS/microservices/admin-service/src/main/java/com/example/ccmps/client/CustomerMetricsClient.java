package com.example.ccmps.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "customer-service", contextId = "customerMetrics")
public interface CustomerMetricsClient {
    @GetMapping("/api/admin/metrics/customers")
    long count();
}
