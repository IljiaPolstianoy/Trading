package io.github.ijlijapol.bybit;


import io.github.ijlijapol.MarketDataFactory;
import io.github.ijlijapol.bybit.exception.TestOrderPersistenceException;
import io.github.ijlijapol.contract.LoaderMarketData;
import io.github.ijlijapol.model.Symbol;
import io.github.ijlijapol.model.order.Side;
import io.github.ijlijapol.model.order.TradeOrderType;
import io.github.ijlijapol.model.request.LastTime;
import io.github.ijlijapol.model.request.RecentMarketData;
import io.github.ijlijapol.model.request.TimeFrame;
import io.github.ijlijapol.model.responce.CandlesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class BybitTestTradeExecutor {

    private CandlesDTO candles;
    private final LoaderMarketData loaderMarketData = MarketDataFactory.getByBitStockMarket();
    private final Repository repository;

    // TODO: при добавление нового метода в LoaderMarketData изменить запрос
    private CandlesDTO getCandles() {
        log.debug("Получения последних трех свечей");
        final RecentMarketData request = RecentMarketData.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .lastTime(LastTime.DAY)
                .build();

        return loaderMarketData.loadRecentMarketData(request);
    }

    private void createTestOrder() {
        log.info("Создания тестового ордера.");
        final BigDecimal lastPrice = candles.getCandles().getLast().getClosePrice();
        final TestOrder testOrder = TestOrder.builder()
                .symbol(Symbol.BTCUSDT)
                .side(Side.BUY)
                .orderType(TradeOrderType.MARKET)
                .price(lastPrice)
                .amount(BigDecimal.ONE)
                .build();

        save(testOrder);
    }

    private void save(final TestOrder testOrder) {
        log.debug("Сохранения тестового ордера в базу данных");
        try {
            repository.save(testOrder);
        } catch (Exception ex) {
            throw new TestOrderPersistenceException("Ошибка сохранения тестового ордера: " +
                    testOrder + "  базу данных", ex);
        }
    }
}
