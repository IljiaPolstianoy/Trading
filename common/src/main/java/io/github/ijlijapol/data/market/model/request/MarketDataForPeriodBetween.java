package io.github.ijlijapol.data.market.model.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@SuperBuilder(toBuilder = true)
public class MarketDataForPeriodBetween extends MarketDataRequest {

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
