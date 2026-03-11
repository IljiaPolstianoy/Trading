package io.github.ijlijapol.data.market.model.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
@ToString
public class RecentMarketData extends MarketDataRequest {

    private final LastTime lastTime;
}
