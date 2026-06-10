package io.github.ilijapol.model;

import io.github.ilijapol.common.model.Side;
import io.github.ilijapol.common.model.Symbol;
import io.github.ilijapol.common.model.TradeOrderType;
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
