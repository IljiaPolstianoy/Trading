package io.github.ijlijapol.data.market;

import io.github.ijlijapol.data.market.model.request.MarketDataForPeriodBetween;
import io.github.ijlijapol.data.market.model.request.MarketDataRequest;
import io.github.ijlijapol.data.market.model.request.RecentMarketData;
import io.github.ijlijapol.data.market.model.responce.CandleDTO;
import io.github.ijlijapol.data.market.model.responce.CandlesDTO;

public interface LoaderMarketData {

    CandlesDTO loadRecentMarketData(RecentMarketData marketDataRequest);

    CandlesDTO loadMarketDateForPeriodBetween(MarketDataForPeriodBetween marketDataRequest);

    CandleDTO loadLatestCandle(MarketDataRequest marketDataRequest);
}
