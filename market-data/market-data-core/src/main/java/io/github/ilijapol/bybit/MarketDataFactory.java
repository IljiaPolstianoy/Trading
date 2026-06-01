package io.github.ilijapol.bybit;

import io.github.ilijapol.common.contract.LoaderMarketData;

public class MarketDataFactory {

    public static LoaderMarketData getByBitStockMarket() {
        return new ByBitLoaderMarketDataImpl();
    }
}
