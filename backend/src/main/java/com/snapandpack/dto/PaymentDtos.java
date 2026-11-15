package com.snapandpack.dto;

import com.snapandpack.model.Payment;
import com.snapandpack.model.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

/** Request and response bodies for M-Pesa payments. */
public final class PaymentDtos {

    private PaymentDtos() {
    }

    /** What "Pay with M-Pesa" sends: only the phone number. The amount comes from the order in the database. */
    public record PayRequest(
            @NotBlank(message = "Enter the M-Pesa phone number")
            @Pattern(regexp = "^(\\+?254|0)[17]\\d{8}$", message = "Enter a Safaricom number such as 0712345678")
            String phone) {
    }

    /** A payment request as the front end sees it. */
    public record PaymentResponse(Long id, Long orderId, BigDecimal amount, PaymentStatus status, String receipt,
                                  String message, boolean simulated) {

        /** Copies a payment from the database into a response. */
        public static PaymentResponse from(Payment p, boolean simulated) {
            return new PaymentResponse(p.getId(), p.getOrder().getId(), p.getAmount(), p.getStatus(),
                    p.getMpesaReceipt(), p.getResultDescription(), simulated);
        }
    }
}
