package com.snapandpack.payment;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Test stand-in for M-Pesa, used while app.mpesa.mode=simulate (the default).
 * No request reaches Safaricom and no money moves. PaymentService confirms each payment itself,
 * through the same code that handles a real Safaricom callback, so the whole flow can be tried out.
 */
@Component
@ConditionalOnProperty(name = "app.mpesa.mode", havingValue = "simulate", matchIfMissing = true)
public class SimulatedMpesaGateway implements MpesaGateway {

    /** Always true for this class. */
    @Override
    public boolean isSimulated() {
        return true;
    }

    /** Invents the ids Safaricom would have returned. */
    @Override
    public StkPushResult requestPayment(String phone, BigDecimal amount, String reference) {
        return new StkPushResult("SIM-" + UUID.randomUUID(), "ws_CO_SIM_" + UUID.randomUUID());
    }

    /** Reports every simulated request as paid. */
    @Override
    public QueryResult query(String checkoutRequestId) {
        return new QueryResult(true, 0, "Simulated payment");
    }
}
