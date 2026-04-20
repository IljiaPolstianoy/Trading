package io.github.ijlijapol.bybit;

import io.github.ijlijapol.contract.LoaderMarketData;

public class MarketDataFactory {

    public static LoaderMarketData getByBitStockMarket () {
        return new ByBitLoaderMarketDataImpl();
    }
}
