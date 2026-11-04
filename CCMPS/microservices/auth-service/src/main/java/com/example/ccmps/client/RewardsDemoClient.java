package com.example.ccmps.client;

import com.example.ccmps.contract.DemoSeedRequest;
import com.example.ccmps.contract.DemoSeedResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "rewards-service", contextId = "rewardsDemo")
public interface RewardsDemoClient {
    @PutMapping("/internal/demo-data")
    DemoSeedResult provision(@RequestBody DemoSeedRequest request,
                             @RequestHeader("Authorization") String authorization);
}
