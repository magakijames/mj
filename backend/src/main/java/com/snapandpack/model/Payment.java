package com.snapandpack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** One M-Pesa payment request for an order, and what became of it. */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    /** Taken from the order in the database, never from the browser. */
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /** The phone the request was sent to, as 2547XXXXXXXX. */
    @Column(name = "phone", nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "merchant_request_id")
    private String merchantRequestId;

    /** Safaricom's id for this request. The callback is matched to the payment by this value. */
    @Column(name = "checkout_request_id", nullable = false, unique = true)
    private String checkoutRequestId;

    /** The M-Pesa receipt number, set when the payment succeeds. */
    @Column(name = "mpesa_receipt")
    private String mpesaReceipt;

    @Column(name = "result_code")
    private Integer resultCode;

    @Column(name = "result_description")
    private String resultDescription;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ---- Getters and setters: plain read and write access to the fields above. ----

    public Long getId() { return id; }

    public CustomerOrder getOrder() { return order; }
    public void setOrder(CustomerOrder order) { this.order = order; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getMerchantRequestId() { return merchantRequestId; }
    public void setMerchantRequestId(String merchantRequestId) { this.merchantRequestId = merchantRequestId; }

    public String getCheckoutRequestId() { return checkoutRequestId; }
    public void setCheckoutRequestId(String checkoutRequestId) { this.checkoutRequestId = checkoutRequestId; }

    public String getMpesaReceipt() { return mpesaReceipt; }
    public void setMpesaReceipt(String mpesaReceipt) { this.mpesaReceipt = mpesaReceipt; }

    public Integer getResultCode() { return resultCode; }
    public void setResultCode(Integer resultCode) { this.resultCode = resultCode; }

    public String getResultDescription() { return resultDescription; }
    public void setResultDescription(String resultDescription) { this.resultDescription = resultDescription; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
