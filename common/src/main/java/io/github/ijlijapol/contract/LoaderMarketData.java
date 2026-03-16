package io.github.ijlijapol.contract;

import io.github.ijlijapol.model.request.MarketDataForPeriodBetween;
import io.github.ijlijapol.model.request.MarketDataRequest;
import io.github.ijlijapol.model.request.RecentMarketData;
import io.github.ijlijapol.model.responce.CandleDTO;
import io.github.ijlijapol.model.responce.CandlesDTO;

public interface LoaderMarketData {

    CandlesDTO loadRecentMarketData(RecentMarketData marketDataRequest);

    CandlesDTO loadMarketDateForPeriodBetween(MarketDataForPeriodBetween marketDataRequest);

    CandleDTO loadLatestCandle(MarketDataRequest marketDataRequest);
}
