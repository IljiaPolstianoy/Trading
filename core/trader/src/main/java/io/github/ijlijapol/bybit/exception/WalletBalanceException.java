package io.github.ijlijapol.bybit.exception;

public class WalletBalanceException extends RuntimeException {
    public WalletBalanceException(String message) {
        super(message);
    }
}
