package io.github.ijlijapol.response;

import com.bybit.api.client.domain.trade.response.OrderResponse;
import io.github.ijlijapol.model.order.Order;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Событие, возникающее при успешном создании ордера на бирже Bybit.
 */
@Getter
public class OrderCreatedEvent extends TradingEvent {

    private final OrderResponse orderResponse;
    private final String orderId;

    public OrderCreatedEvent(
            final Object source,
            final Order originalOrder,
            final OrderResponse orderResponse,
            final LocalDateTime eventTimestamp) {
        super(source, originalOrder, EventType.ORDER_CREATED, eventTimestamp
        );
        this.orderResponse = orderResponse;
        this.orderId = orderResponse.getOrderId();
    }
}
