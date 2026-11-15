package com.snapandpack.repository;

import com.snapandpack.model.Payment;
import com.snapandpack.model.PaymentStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for payments. */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Finds the payment a callback from Safaricom belongs to. */
    Optional<Payment> findByCheckoutRequestId(String checkoutRequestId);

    /** The most recent payment request for an order. */
    Optional<Payment> findFirstByOrderIdOrderByIdDesc(Long orderId);

    /** True when the order already has a request with this status made after the given time. */
    boolean existsByOrderIdAndStatusAndCreatedAtAfter(Long orderId, PaymentStatus status, LocalDateTime after);
}
