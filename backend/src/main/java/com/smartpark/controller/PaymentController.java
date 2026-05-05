package com.smartpark.controller;

import com.smartpark.dto.PaymentRequest;
import com.smartpark.dto.PaymentResponse;
import com.smartpark.security.JwtTokenProvider;
import com.smartpark.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtTokenProvider tokenProvider;

    public PaymentController(PaymentService paymentService, JwtTokenProvider tokenProvider) {
        this.paymentService = paymentService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        PaymentResponse response = paymentService.createPaymentIntent(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        PaymentResponse response = paymentService.confirmPayment(userId, request);
        return ResponseEntity.ok(response);
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return tokenProvider.getUserIdFromToken(token);
    }
}
