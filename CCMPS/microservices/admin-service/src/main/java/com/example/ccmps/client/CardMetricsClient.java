package com.example.ccmps.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "card-service", contextId = "cardMetrics")
public interface CardMetricsClient {
    @GetMapping("/api/admin/metrics/cards")
    Map<String, Long> counts();
}
