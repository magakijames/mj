package com.snapandpack.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.snapandpack.dto.PaymentDtos.PayRequest;
import com.snapandpack.dto.PaymentDtos.PaymentResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.security.CurrentUser;
import com.snapandpack.service.PaymentService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Addresses for M-Pesa payments. */
@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;
    private final String callbackSecret;

    /** Receives the payment service and the secret that forms part of the callback address. */
    public PaymentController(PaymentService paymentService, @Value("${app.mpesa.callback-secret}") String callbackSecret) {
        this.paymentService = paymentService;
        this.callbackSecret = callbackSecret;
    }

    /** POST /api/orders/{id}/pay: sends the M-Pesa prompt for the user's own order to the given phone. */
    @PostMapping("/orders/{id}/pay")
    public PaymentResponse pay(Authentication authentication, @PathVariable Long id, @Valid @RequestBody PayRequest request) {
        return paymentService.start(CurrentUser.id(authentication), id, request.phone());
    }

    /** POST /api/orders/{id}/payment/check: asks M-Pesa how the latest payment ended. The order's owner, or order staff. */
    @PostMapping("/orders/{id}/payment/check")
    public PaymentResponse check(Authentication authentication, @PathVariable Long id) {
        return paymentService.check(CurrentUser.id(authentication), id);
    }

    /**
     * POST /api/payments/mpesa/callback/{secret}: where Safaricom reports the result of a payment.
     * Safaricom cannot sign in, so this address is open; it is protected by the secret in the address
     * (known only to this server and Safaricom) and by matching each report to a request this shop made.
     * The reply tells Safaricom the report was received.
     */
    @PostMapping("/payments/mpesa/callback/{secret}")
    public Map<String, Object> mpesaCallback(@PathVariable String secret, @RequestBody JsonNode body) {
        boolean secretMatches = !callbackSecret.isBlank() && MessageDigest.isEqual(
                secret.getBytes(StandardCharsets.UTF_8), callbackSecret.getBytes(StandardCharsets.UTF_8));
        if (!secretMatches) {
            throw ApiException.notFound("Not found");
        }
        paymentService.handleCallback(body);
        return Map.of("ResultCode", 0, "ResultDesc", "Accepted");
    }
}
