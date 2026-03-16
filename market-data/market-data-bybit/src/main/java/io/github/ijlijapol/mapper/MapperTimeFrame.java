package io.github.ijlijapol.mapper;

import com.bybit.api.client.domain.market.MarketInterval;
import io.github.ijlijapol.model.request.TimeFrame;

public class MapperTimeFrame {

    public static MarketInterval toMarketInterval(final TimeFrame timeFrame) {
        return switch (timeFrame) {
            case FIFTEEN_MINUTES -> MarketInterval.FIFTEEN_MINUTES;
        };
    }
}