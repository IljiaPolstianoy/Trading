package io.github.ijlijapol.bybit.model.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
@ToString
public class SelectQuantityCandleRequest extends BaseDataRequest {
    private final int quantity;
}
