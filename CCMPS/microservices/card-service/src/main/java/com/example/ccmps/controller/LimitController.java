package com.example.ccmps.controller;

import com.example.ccmps.dto.LimitRequestDto;
import com.example.ccmps.entity.LimitRequest;
import com.example.ccmps.service.LimitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

        import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/limits")
@RequiredArgsConstructor
public class LimitController {

    private final LimitService limitService;

    @PostMapping("/requests")
    public LimitRequest createRequest(
            @Valid
            @RequestBody
            LimitRequestDto request) {

        return limitService.createRequest(request);
    }

    @GetMapping("/requests")
    public List<LimitRequest> getRequests(
            @RequestParam UUID accountId) {

        return limitService.getRequests(accountId);
    }
}