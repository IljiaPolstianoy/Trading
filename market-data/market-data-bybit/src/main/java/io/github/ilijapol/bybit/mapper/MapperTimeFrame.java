package io.github.ilijapol.bybit.mapper;

import com.bybit.api.client.domain.market.MarketInterval;
import io.github.ilijapol.common.model.TimeFrame;

public class MapperTimeFrame {

    public static MarketInterval toMarketInterval(final TimeFrame timeFrame) {
        return switch (timeFrame) {
            case ONE_MINUTE -> MarketInterval.ONE_MINUTE;
            case FIVE_MINUTES -> MarketInterval.FIVE_MINUTES;
            case FIFTEEN_MINUTES -> MarketInterval.FIFTEEN_MINUTES;
            case THIRTY_MINUTES -> MarketInterval.HALF_HOURLY;
            case ONE_HOUR -> MarketInterval.HOURLY;
            case FOUR_HOURS -> MarketInterval.FOUR_HOURLY;
            case ONE_DAY -> MarketInterval.DAILY;
        };
    }
}