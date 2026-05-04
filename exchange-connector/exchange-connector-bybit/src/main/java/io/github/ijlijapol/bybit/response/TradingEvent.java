package io.github.ijlijapol.bybit.response;

import io.github.ijlijapol.bybit.model.order.OrderDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class TradingEvent extends ApplicationEvent {

    private final OrderDTO originalOrderDTO;
    private final EventType eventType;
    private final LocalDateTime eventTimestamp;

    protected TradingEvent(
            final Object source,
            final OrderDTO originalOrderDTO,
            final EventType eventType,
            final LocalDateTime eventTimestamp) {
        super(source);
        this.originalOrderDTO = originalOrderDTO;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
    }
}
