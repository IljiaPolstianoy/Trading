package io.github.ijlijapol.bybit.model;

import io.github.ijlijapol.bybit.model.order.Side;
import io.github.ijlijapol.bybit.model.order.TradeOrderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder(toBuilder = true)
@ToString
@Table(name = "orders")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Symbol symbol;

    @Column(name = "side")
    private Side side;

    @Column(name = "order_type")
    private TradeOrderType orderType;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "amount")
    private BigDecimal amount;

}
