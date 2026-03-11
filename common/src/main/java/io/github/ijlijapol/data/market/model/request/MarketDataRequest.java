package io.github.ijlijapol.data.market.model.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
@Getter
@ToString
public class MarketDataRequest {

    protected Symbol symbol;

    protected TimeFrame timeFrame;

}
