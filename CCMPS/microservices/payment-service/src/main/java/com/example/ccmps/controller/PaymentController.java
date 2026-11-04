package com.example.ccmps.controller;

import com.example.ccmps.dto.CreatePaymentRequest;
import com.example.ccmps.dto.PaymentResponse;
import com.example.ccmps.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(

            @Valid
            @RequestBody
            CreatePaymentRequest request,

            @RequestHeader("Idempotency-Key")
            @NotBlank
            String idempotencyKey) {

        PaymentResponse response =
                paymentService.createPayment(
                        request,
                        idempotencyKey
                );

        return ResponseEntity
                .accepted()
                .body(response);
    }

    @GetMapping
    public List<PaymentResponse> getPayments(
            @RequestParam UUID accountId) {

        return paymentService.getPayments(accountId);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(
            @PathVariable UUID paymentId) {

        return paymentService.getPayment(paymentId);
    }
}