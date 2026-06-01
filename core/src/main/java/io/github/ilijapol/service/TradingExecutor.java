package io.github.ilijapol.service;

/**
 * Интерфейс являющийся спецификацией торговых исполнителей. Имеет только один публичный метод, {@link #start()}.
 * Вся бизнес-логика должна быть скрыта внутри.
 */
public interface TradingExecutor {

    void start();

}
