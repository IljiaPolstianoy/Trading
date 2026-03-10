package io.github.ijlijapol;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.service.BybitApiClientFactory;
import io.github.ijlijapol.data.market.model.request.MarketDataForPeriodBetween;
import io.github.ijlijapol.data.market.model.request.Symbol;
import io.github.ijlijapol.data.market.model.request.TimeFrame;
import io.github.ijlijapol.data.market.model.responce.CandlesDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Test {
    public static void load() {
        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();
        var tickerRequest = MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol("BTCUSDT")
                .build();
        var tickerHistoryRequest = MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol("BTCUSDT")
                .marketInterval(MarketInterval.WEEKLY)
                .start(LocalDateTime.of(2026, 2, 1, 0, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli())
                .end(Instant.now().toEpochMilli())
                .limit(1)
                .build();
//        KlineResponse tickers1 = MapperByBitData.getKlineResponse(client.getMarketLinesData(tickerRequest));
        var tickers = client.getMarketTickers(tickerRequest);
        System.out.println(tickers + "\n");
        var tickersHistory = client.getMarketLinesData(tickerHistoryRequest);
        System.out.println(tickersHistory);
    }

    public static void main(String[] args) {
//        load();
        ByBitLoaderMarketDataImpl byBitLoaderMarketData = new ByBitLoaderMarketDataImpl();
        CandlesDTO candlesDTO = byBitLoaderMarketData.loadMarketDateForPeriodBetween(
                MarketDataForPeriodBetween.builder()
                        .symbol(Symbol.BTCUSDT)
                        .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                        .startTime(LocalDateTime.of(2026, 2, 1, 0, 0, 0))
                        .endTime(LocalDateTime.of(2026, 2, 2, 0, 0, 0))
                        .build()
        );
        System.out.println(candlesDTO);
    }
}
