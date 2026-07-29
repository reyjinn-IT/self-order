package com.tableorder.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Validasi signature_key notifikasi Midtrans.
 * Formula resmi Midtrans:
 *   SHA512(order_id + status_code + gross_amount + ServerKey)
 */
@Component
public class MidtransSignatureValidator {

    public boolean isValid(String orderId, String statusCode, String grossAmount,
                            String serverKey, String signatureKey) {
        if (orderId == null || statusCode == null || grossAmount == null
                || serverKey == null || signatureKey == null) {
            return false;
        }
        String raw = orderId + statusCode + grossAmount + serverKey;
        String computed = sha512(raw);
        return computed.equalsIgnoreCase(signatureKey);
    }

    private String sha512(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 algorithm not available", e);
        }
    }
}
