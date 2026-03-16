package io.github.ijlijapol.model.request;

import io.github.ijlijapol.model.Symbol;
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
