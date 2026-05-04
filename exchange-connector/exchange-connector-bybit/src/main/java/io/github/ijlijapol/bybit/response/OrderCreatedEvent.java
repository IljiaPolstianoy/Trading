package io.github.ijlijapol.bybit.response;

import com.bybit.api.client.domain.trade.response.OrderResponse;
import io.github.ijlijapol.bybit.model.order.OrderDTO;
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
            final OrderDTO originalOrderDTO,
            final OrderResponse orderResponse,
            final LocalDateTime eventTimestamp) {
        super(source, originalOrderDTO, EventType.ORDER_CREATED, eventTimestamp
        );
        this.orderResponse = orderResponse;
        this.orderId = orderResponse.getOrderId();
    }
}
