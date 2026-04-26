package io.github.ijlijapol.bybit.model;

import io.github.ijlijapol.bybit.model.order.Side;
import io.github.ijlijapol.bybit.model.order.TradeOrderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Entity
@Table(name = "test_orders")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "symbol", nullable = false)
    @Enumerated(EnumType.STRING)
    private Symbol symbol;

    @Column(name = "side", nullable = false)
    @Enumerated(EnumType.STRING)
    private Side side;

    @Column(name = "order_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TradeOrderType orderType;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

}
