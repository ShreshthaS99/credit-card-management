package com.example.ccmps.controller;

import com.example.ccmps.contract.DemoSeedRequest;
import com.example.ccmps.contract.DemoSeedResult;
import com.example.ccmps.service.DemoDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DemoDataController {
    private final DemoDataService demoData;

    @PutMapping("/internal/demo-data")
    public DemoSeedResult provision(@Valid @RequestBody DemoSeedRequest request) {
        return demoData.provision(request);
    }
}
