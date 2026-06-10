package io.github.ilijapol.bybit.mapper;

import com.bybit.api.client.domain.market.MarketInterval;
import io.github.ilijapol.common.model.TimeFrame;

public class MapperTimeFrame {

    public static MarketInterval toMarketInterval(final TimeFrame timeFrame) {
        return switch (timeFrame) {
            case ONE_MINUTE -> MarketInterval.ONE_MINUTE;
            case FIFTEEN_MINUTES -> MarketInterval.FIFTEEN_MINUTES;
        };
    }
}