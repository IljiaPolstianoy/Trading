package io.github.ijlijapol.contract;

import io.github.ijlijapol.bybit.model.request.LastCandleRequest;
import io.github.ijlijapol.bybit.model.request.MarketDataForPeriodBetweenRequest;
import io.github.ijlijapol.bybit.model.request.RecentMarketDataRequest;
import io.github.ijlijapol.bybit.model.request.SelectQuantityCandleRequest;
import io.github.ijlijapol.bybit.model.responce.CandleDTO;
import io.github.ijlijapol.bybit.model.responce.CandlesDTO;

/**
 * Контракт для загрузки рыночных данных (свечей) из источника данных.
 */
public interface LoaderMarketData {

    /**
     * Загружает коллекцию последних рыночных свечей согласно параметрам запроса.
     * Количество свечей и их таймфрейм определяются в объекте запроса.
     *
     * @param marketDataRequest запрос, содержащий параметры (символ, таймфрейм, за какое последнее время запрос)
     * @return объект {@link CandlesDTO}, содержащий список запрошенных свечей
     */
    CandlesDTO loadRecentMarketData(RecentMarketDataRequest marketDataRequest);

    /**
     * Загружает рыночные свечи за указанный временной период между двумя датами.
     *
     * @param marketDataRequest запрос, содержащий символ, таймфрейм, начальную и конечную даты периода
     * @return объект {@link CandlesDTO}, содержащий свечи, попадающие в заданный интервал
     */
    CandlesDTO loadMarketDateForPeriodBetween(MarketDataForPeriodBetweenRequest marketDataRequest);

    /**
     * Загружает последнюю (самую свежую) завершённую свечу.
     *
     * @param marketDataRequest запрос, содержащий символ и таймфрейм свечи
     * @return объект {@link CandleDTO} с данными последней свечи
     */
    CandleDTO loadLatestCandle(LastCandleRequest marketDataRequest);

    /**
     * Загружает выбранное количчество последних свечей
     *
     * @param marketDataRequest запрос, содержащий символ, таймфрен свечи и количество свечей
     * @return объект {@link CandlesDTO} с данным последних свечей
     */
    CandlesDTO loadSelectQuantityCandle(SelectQuantityCandleRequest marketDataRequest);
}
