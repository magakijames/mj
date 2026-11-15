package com.snapandpack.payment;

import java.math.BigDecimal;

/**
 * The link to M-Pesa. Two implementations exist:
 *   SimulatedMpesaGateway - for testing; pretends the customer paid, without contacting Safaricom
 *   DarajaMpesaGateway    - the real one; talks to Safaricom's Daraja API
 * app.mpesa.mode in application.properties chooses between them.
 */
public interface MpesaGateway {

    /** What Safaricom answers when a payment request is accepted for sending to the phone. */
    record StkPushResult(String merchantRequestId, String checkoutRequestId) {
    }

    /** What Safaricom answers when asked how a request ended. finished is false while the customer has not answered. */
    record QueryResult(boolean finished, int resultCode, String description) {
    }

    /** True when no real money moves and payments confirm themselves. */
    boolean isSimulated();

    /**
     * Sends a payment prompt (STK push) to the customer's phone.
     *   phone      as 2547XXXXXXXX
     *   amount     whole shillings to charge
     *   reference  shown to the customer, for example "Order 12"
     */
    StkPushResult requestPayment(String phone, BigDecimal amount, String reference);

    /** Asks Safaricom what became of an earlier request. Used when no callback arrived. */
    QueryResult query(String checkoutRequestId);
}
