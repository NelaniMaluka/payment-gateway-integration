package com.nelani.demo.exception;

/**
 * Indicates a temporary failure when communicating with a payment provider.
 *
 * <p>
 * This exception is used to trigger retry logic (e.g. Spring Retry)
 * for transient provider errors such as timeouts or 5xx responses.
 * </p>
 *
 * <p>
 * This exception must NOT be used for permanent or business-logic failures.
 * </p>
 */
public class PaymentProviderTemporaryException extends RuntimeException {

    public PaymentProviderTemporaryException(String message) {
        super(message);
    }
}
