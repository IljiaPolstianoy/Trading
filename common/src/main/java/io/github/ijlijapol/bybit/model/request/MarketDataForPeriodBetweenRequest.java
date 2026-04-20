package io.github.ijlijapol.bybit.model.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
@SuperBuilder(toBuilder = true)
public class MarketDataForPeriodBetweenRequest extends BaseDataRequest {

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
