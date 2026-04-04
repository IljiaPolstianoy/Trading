package io.github.ijlijapol.contract;

import io.github.ijlijapol.model.request.LastCandleRequest;
import io.github.ijlijapol.model.request.MarketDataForPeriodBetweenRequest;
import io.github.ijlijapol.model.request.RecentMarketDataRequest;
import io.github.ijlijapol.model.responce.CandleDTO;
import io.github.ijlijapol.model.responce.CandlesDTO;

public interface LoaderMarketData {

    CandlesDTO loadRecentMarketData(RecentMarketDataRequest marketDataRequest);

    CandlesDTO loadMarketDateForPeriodBetween(MarketDataForPeriodBetweenRequest marketDataRequest);

    CandleDTO loadLatestCandle(LastCandleRequest marketDataRequest);
}
