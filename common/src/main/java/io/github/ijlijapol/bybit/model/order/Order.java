package io.github.ijlijapol.bybit.model.order;

import io.github.ijlijapol.bybit.model.Symbol;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
public class Order {

    private Symbol symbol;

    private Side side;

    private TradeOrderType orderType;

    private BigDecimal price;

    private BigDecimal amount;

}
