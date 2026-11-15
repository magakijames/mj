package com.snapandpack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.snapandpack.dto.PaymentDtos.PaymentResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.model.CustomerOrder;
import com.snapandpack.model.OrderStatus;
import com.snapandpack.model.Payment;
import com.snapandpack.model.PaymentStatus;
import com.snapandpack.model.Role;
import com.snapandpack.model.User;
import com.snapandpack.payment.MpesaGateway;
import com.snapandpack.payment.MpesaGateway.QueryResult;
import com.snapandpack.payment.MpesaGateway.StkPushResult;
import com.snapandpack.repository.OrderRepository;
import com.snapandpack.repository.PaymentRepository;
import com.snapandpack.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * M-Pesa payments for orders. The rules that keep money safe:
 *   - the amount always comes from the order in the database, never from the browser
 *   - an order is marked paid only when Safaricom confirms it (the callback, or a status query),
 *     never because the browser says so
 *   - a confirmation is matched to the request we sent, its amount is checked, and a confirmation
 *     that arrives twice is ignored the second time
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository payments;
    private final OrderRepository orders;
    private final UserRepository users;
    private final MpesaGateway gateway;
    private final NotificationService notifications;
    private final AuditService audit;
    private final boolean autoConfirmWhenSimulated;

    /** Receives the services and database access this class needs. */
    public PaymentService(PaymentRepository payments, OrderRepository orders, UserRepository users,
                          MpesaGateway gateway, NotificationService notifications, AuditService audit,
                          @Value("${app.mpesa.simulate-auto-confirm}") boolean autoConfirmWhenSimulated) {
        this.payments = payments;
        this.orders = orders;
        this.users = users;
        this.gateway = gateway;
        this.notifications = notifications;
        this.audit = audit;
        this.autoConfirmWhenSimulated = autoConfirmWhenSimulated;
    }

    /**
     * Starts a payment: sends the M-Pesa prompt to the customer's phone for the order's total.
     * Only the customer who owns the order may pay for it. In test mode the payment then confirms itself.
     */
    @Transactional
    public PaymentResponse start(Long userId, Long orderId, String phone) {
        CustomerOrder order = findOwnOrder(userId, orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw ApiException.badRequest("This order was cancelled");
        }
        if (order.isPaid()) {
            throw ApiException.conflict("This order is already paid");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (payments.existsByOrderIdAndStatusAndCreatedAtAfter(orderId, PaymentStatus.PENDING, now.minusMinutes(2))) {
            throw ApiException.conflict("A payment request is already waiting on your phone. Finish it, or wait two minutes and try again.");
        }

        String msisdn = toMsisdn(phone);
        StkPushResult sent = gateway.requestPayment(msisdn, order.getTotal(), "Order " + order.getId());

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotal());
        payment.setPhone(msisdn);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setMerchantRequestId(sent.merchantRequestId());
        payment.setCheckoutRequestId(sent.checkoutRequestId());
        payment.setCreatedAt(now);
        payments.save(payment);
        audit.record("PAYMENT_REQUESTED", "Order #" + order.getId() + ", " + money(order.getTotal()));

        if (gateway.isSimulated() && autoConfirmWhenSimulated) {
            String receipt = "SIM" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            finish(payment, 0, "Simulated payment (test mode)", order.getTotal(), receipt);
        }
        return PaymentResponse.from(payment, gateway.isSimulated());
    }

    /**
     * Handles the message Safaricom sends when the customer has answered the prompt.
     * Unknown requests are ignored, so a made-up callback cannot mark anything as paid.
     */
    @Transactional
    public void handleCallback(JsonNode body) {
        JsonNode callback = body.path("Body").path("stkCallback");
        String checkoutRequestId = callback.path("CheckoutRequestID").asText("");
        Payment payment = payments.findByCheckoutRequestId(checkoutRequestId).orElse(null);
        if (payment == null) {
            log.warn("M-Pesa callback for a payment request this shop never made was ignored");
            return;
        }

        BigDecimal amount = null;
        String receipt = null;
        for (JsonNode item : callback.path("CallbackMetadata").path("Item")) {
            String name = item.path("Name").asText();
            if (name.equals("Amount")) {
                amount = item.path("Value").decimalValue();
            } else if (name.equals("MpesaReceiptNumber")) {
                receipt = item.path("Value").asText();
            }
        }
        finish(payment, callback.path("ResultCode").asInt(-1), callback.path("ResultDesc").asText(""), amount, receipt);
    }

    /**
     * Asks M-Pesa what became of an order's latest payment request, for when no callback arrived.
     * The order's owner and staff who handle orders may ask.
     */
    @Transactional
    public PaymentResponse check(Long userId, Long orderId) {
        User asker = users.findById(userId).orElseThrow(() -> ApiException.notFound("Account not found"));
        CustomerOrder order = Role.ORDER_STAFF.contains(asker.getRole())
                ? orders.findById(orderId).orElseThrow(() -> ApiException.notFound("Order not found"))
                : findOwnOrder(userId, orderId);
        Payment payment = payments.findFirstByOrderIdOrderByIdDesc(order.getId())
                .orElseThrow(() -> ApiException.notFound("No payment has been started for this order"));

        if (payment.getStatus() == PaymentStatus.PENDING) {
            QueryResult result = gateway.query(payment.getCheckoutRequestId());
            if (result.finished()) {
                // A status query confirms the request we made for this amount; it carries no receipt number.
                finish(payment, result.resultCode(), result.description(), payment.getAmount(), null);
            }
        }
        return PaymentResponse.from(payment, gateway.isSimulated());
    }

    /**
     * Records how a payment request ended. Does nothing if it was already recorded, so the same
     * confirmation arriving twice cannot be counted twice. On success the order is marked paid
     * and the customer and staff are told.
     */
    private void finish(Payment payment, int resultCode, String description, BigDecimal paidAmount, String receipt) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }
        CustomerOrder order = payment.getOrder();
        User customer = order.getUser();
        payment.setResultCode(resultCode);
        payment.setResultDescription(description.length() > 255 ? description.substring(0, 255) : description);
        payment.setCompletedAt(LocalDateTime.now(ZoneOffset.UTC));

        if (resultCode != 0) {
            payment.setStatus(PaymentStatus.FAILED);
            audit.record(customer, "PAYMENT_FAILED", "Order #" + order.getId() + ": " + payment.getResultDescription());
            notifications.notify(customer, "Payment not completed",
                    "The M-Pesa payment for order #" + order.getId() + " did not go through. You can try again from My orders.",
                    "pages/orders.html");
            return;
        }

        // M-Pesa charges whole shillings, so compare against the order total rounded up.
        BigDecimal expected = payment.getAmount().setScale(0, RoundingMode.UP);
        if (paidAmount == null || paidAmount.compareTo(expected) != 0) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setResultDescription("Amount received does not match the order total");
            audit.record(customer, "PAYMENT_MISMATCH", "Order #" + order.getId() + ": expected " + money(expected)
                    + ", M-Pesa reported " + (paidAmount == null ? "nothing" : money(paidAmount)));
            notifications.notifyStaff(Role.ORDER_STAFF, "Payment needs checking",
                    "Order #" + order.getId() + ": the amount M-Pesa reported does not match the order total.", "admin/orders.html");
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setMpesaReceipt(receipt);
        if (order.isPaid()) {
            audit.record(customer, "PAYMENT_DUPLICATE", "Order #" + order.getId() + " was paid a second time, receipt " + receipt);
            notifications.notifyStaff(Role.ORDER_STAFF, "Order paid twice",
                    "Order #" + order.getId() + " received a second payment (" + receipt + "). A refund may be due.", "admin/orders.html");
            return;
        }
        order.setPaymentStatus("PAID");
        order.setPaymentReference(receipt == null ? "Confirmed by M-Pesa" : receipt);
        audit.record(customer, "PAYMENT_RECEIVED", "Order #" + order.getId() + ", " + money(expected) + ", receipt " + order.getPaymentReference());
        notifications.notify(customer, "Payment received",
                "We received " + money(expected) + " for order #" + order.getId() + ". Receipt: " + order.getPaymentReference() + ".",
                "pages/orders.html");
        notifications.notifyStaff(Role.ORDER_STAFF, "Order #" + order.getId() + " paid",
                customer.getName() + " paid " + money(expected) + ".", "admin/orders.html");
    }

    /** Loads an order only if it belongs to this user. Someone else's order looks exactly like a missing one. */
    private CustomerOrder findOwnOrder(Long userId, Long orderId) {
        CustomerOrder order = orders.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getId().equals(userId)) {
            throw ApiException.notFound("Order not found");
        }
        return order;
    }

    /** Turns 0712345678, +254712345678 or 254712345678 into the form M-Pesa wants: 254712345678. */
    static String toMsisdn(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("0")) {
            digits = "254" + digits.substring(1);
        }
        if (!digits.matches("254[17]\\d{8}")) {
            throw ApiException.badRequest("Enter a Safaricom number such as 0712345678");
        }
        return digits;
    }

    /** Formats an amount for messages, for example "KSh 1,500". */
    static String money(BigDecimal amount) {
        return String.format("KSh %,.0f", amount);
    }
}
