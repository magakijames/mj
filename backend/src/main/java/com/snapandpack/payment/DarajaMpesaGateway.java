package com.snapandpack.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.snapandpack.exception.ApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * The real link to M-Pesa through Safaricom's Daraja API ("M-Pesa Express", also called STK push).
 * Used when app.mpesa.mode=live.
 *
 * NOT YET TESTED AGAINST SAFARICOM. It follows the published Daraja API, but must be tried in the
 * Daraja sandbox before it is trusted with real money.
 *
 * TO SWITCH ON:
 *   1. Create an account at https://developer.safaricom.co.ke and create an app there.
 *      Copy its Consumer Key and Consumer Secret.
 *   2. For sandbox testing use the test shortcode 174379 and the test passkey shown on the
 *      "M-Pesa Express" simulator page. For real payments use your own Paybill or Till number and
 *      the passkey Safaricom emails you after "Go Live".
 *   3. Set these as ENVIRONMENT VARIABLES (never write them into a file that is saved to Git):
 *        MPESA_MODE=live
 *        MPESA_BASE_URL=https://sandbox.safaricom.co.ke      (testing)
 *                       https://api.safaricom.co.ke          (real payments)
 *        MPESA_CONSUMER_KEY=...
 *        MPESA_CONSUMER_SECRET=...
 *        MPESA_SHORTCODE=174379
 *        MPESA_PASSKEY=...
 *        MPESA_TRANSACTION_TYPE=CustomerPayBillOnline        (Paybill)  or  CustomerBuyGoodsOnline  (Till)
 *        MPESA_CALLBACK_SECRET=a long random text of your own
 *        PUBLIC_URL=https://your-public-address
 *   4. Safaricom must be able to reach the callback address over HTTPS from the internet:
 *        PUBLIC_URL + /api/payments/mpesa/callback/ + MPESA_CALLBACK_SECRET
 *      On your own computer nothing on the internet can reach localhost, so for testing run a tunnel
 *      such as ngrok ("ngrok http 8080") and put the https address it prints into PUBLIC_URL.
 */
@Component
@ConditionalOnProperty(name = "app.mpesa.mode", havingValue = "live")
public class DarajaMpesaGateway implements MpesaGateway {

    private static final Logger log = LoggerFactory.getLogger(DarajaMpesaGateway.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");

    private final RestClient http;
    private final String consumerKey;
    private final String consumerSecret;
    private final String shortcode;
    private final String passkey;
    private final String transactionType;
    private final String callbackUrl;

    /** Receives the Daraja settings from application.properties (which reads them from environment variables). */
    public DarajaMpesaGateway(@Value("${app.mpesa.base-url}") String baseUrl,
                              @Value("${app.mpesa.consumer-key}") String consumerKey,
                              @Value("${app.mpesa.consumer-secret}") String consumerSecret,
                              @Value("${app.mpesa.shortcode}") String shortcode,
                              @Value("${app.mpesa.passkey}") String passkey,
                              @Value("${app.mpesa.transaction-type}") String transactionType,
                              @Value("${app.mpesa.callback-secret}") String callbackSecret,
                              @Value("${app.public-url}") String publicUrl) {
        if (consumerKey.isBlank() || consumerSecret.isBlank() || shortcode.isBlank() || passkey.isBlank()
                || callbackSecret.isBlank() || publicUrl.isBlank()) {
            throw new IllegalStateException("app.mpesa.mode=live needs MPESA_CONSUMER_KEY, MPESA_CONSUMER_SECRET, "
                    + "MPESA_SHORTCODE, MPESA_PASSKEY, MPESA_CALLBACK_SECRET and PUBLIC_URL to be set");
        }
        this.http = RestClient.builder().baseUrl(baseUrl).build();
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.shortcode = shortcode;
        this.passkey = passkey;
        this.transactionType = transactionType;
        this.callbackUrl = publicUrl.replaceAll("/+$", "") + "/api/payments/mpesa/callback/" + callbackSecret;
    }

    /** Always false for this class: real requests go to Safaricom. */
    @Override
    public boolean isSimulated() {
        return false;
    }

    /** Asks Safaricom to show the "enter your M-Pesa PIN" prompt on the customer's phone. */
    @Override
    public StkPushResult requestPayment(String phone, BigDecimal amount, String reference) {
        String timestamp = timestamp();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("BusinessShortCode", shortcode);
        body.put("Password", password(timestamp));
        body.put("Timestamp", timestamp);
        body.put("TransactionType", transactionType);
        // M-Pesa takes whole shillings only.
        body.put("Amount", amount.setScale(0, RoundingMode.UP).toPlainString());
        body.put("PartyA", phone);
        body.put("PartyB", shortcode);
        body.put("PhoneNumber", phone);
        body.put("CallBackURL", callbackUrl);
        body.put("AccountReference", reference);
        body.put("TransactionDesc", reference);

        JsonNode reply = post("/mpesa/stkpush/v1/processrequest", body);
        if (!"0".equals(reply.path("ResponseCode").asText())) {
            log.warn("M-Pesa refused a payment request: {}", reply.path("ResponseDescription").asText());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "M-Pesa could not start the payment. Try again shortly.");
        }
        return new StkPushResult(reply.path("MerchantRequestID").asText(), reply.path("CheckoutRequestID").asText());
    }

    /** Asks Safaricom how an earlier request ended, for when the callback never arrived. */
    @Override
    public QueryResult query(String checkoutRequestId) {
        String timestamp = timestamp();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("BusinessShortCode", shortcode);
        body.put("Password", password(timestamp));
        body.put("Timestamp", timestamp);
        body.put("CheckoutRequestID", checkoutRequestId);

        JsonNode reply;
        try {
            reply = post("/mpesa/stkpushquery/v1/query", body);
        } catch (ApiException e) {
            // Safaricom answers with an error while the customer is still looking at the prompt.
            return new QueryResult(false, -1, "Still waiting for the customer");
        }
        if (!reply.has("ResultCode")) {
            return new QueryResult(false, -1, "Still waiting for the customer");
        }
        return new QueryResult(true, reply.path("ResultCode").asInt(-1), reply.path("ResultDesc").asText());
    }

    /** Gets the short-lived access token Safaricom requires on every call, using the consumer key and secret. */
    private String accessToken() {
        String basic = Base64.getEncoder().encodeToString((consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));
        try {
            JsonNode reply = http.get().uri("/oauth/v1/generate?grant_type=client_credentials")
                    .header("Authorization", "Basic " + basic)
                    .retrieve().body(JsonNode.class);
            return reply.path("access_token").asText();
        } catch (RestClientException e) {
            log.error("Could not sign in to M-Pesa: {}", e.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "M-Pesa is not reachable right now. Try again shortly.");
        }
    }

    /** Sends one JSON request to Daraja and returns the JSON answer. Secrets are never written to the log. */
    private JsonNode post(String path, Map<String, Object> body) {
        try {
            return http.post().uri(path)
                    .header("Authorization", "Bearer " + accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve().body(JsonNode.class);
        } catch (RestClientException e) {
            log.warn("M-Pesa call to {} failed: {}", path, e.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "M-Pesa is not reachable right now. Try again shortly.");
        }
    }

    /** The current Nairobi time in the form Daraja expects, for example 20261115143000. */
    private static String timestamp() {
        return ZonedDateTime.now(NAIROBI).format(TIMESTAMP);
    }

    /** The per-request password Daraja expects: shortcode + passkey + timestamp, Base64 encoded. */
    private String password(String timestamp) {
        return Base64.getEncoder().encodeToString((shortcode + passkey + timestamp).getBytes(StandardCharsets.UTF_8));
    }
}
