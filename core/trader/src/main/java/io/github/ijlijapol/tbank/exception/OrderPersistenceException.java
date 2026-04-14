package io.github.ijlijapol.tbank.exception;

public class OrderPersistenceException extends RuntimeException {
    public OrderPersistenceException(String message, Exception cause) {
        super(message, cause);
    }
}
