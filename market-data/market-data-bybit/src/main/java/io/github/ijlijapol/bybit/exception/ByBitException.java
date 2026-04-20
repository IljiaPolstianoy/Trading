package io.github.ijlijapol.bybit.exception;

public class ByBitException extends RuntimeException {
    public ByBitException(String message, Exception cause) {
        super(message, cause);
    }
}
