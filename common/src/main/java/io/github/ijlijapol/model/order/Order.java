package io.github.ijlijapol.model.order;

import io.github.ijlijapol.model.Symbol;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class Order {

    private Symbol symbol;

    private Side side;

    private TradeOrderType orderType;

    private BigDecimal price;

    private BigDecimal amount;

}
