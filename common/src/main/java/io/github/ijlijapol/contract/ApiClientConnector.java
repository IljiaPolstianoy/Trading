package io.github.ijlijapol.contract;

import java.math.BigDecimal;


/**
 * Клиент для работы с кошельком
 */
public interface ApiClientConnector {

    /**
     * Отправляет запрос брокеру/брижи на получение количества в кошельшке выбранного актива {@code active}
     *
     * @param active Актив по которому запрашиваются сведения
     * @return Количество запрашиваемого актива
     */
    BigDecimal getWalletActiveBalance(String active);
}
