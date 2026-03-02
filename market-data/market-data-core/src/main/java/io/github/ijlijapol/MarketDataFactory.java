package io.github.ijlijapol;

import io.github.ijlijapol.data.market.LoaderMarketData;

public class MarketDataFactory {

    public static LoaderMarketData getByBitStockMarket () {
        return new ByBitLoaderMarketDataImpl();
    }
}
