package io.github.ijlijapol.response;

import io.github.ijlijapol.model.order.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class TradingEvent extends ApplicationEvent {

    private final Order originalOrder;
    private final EventType eventType;
    private final LocalDateTime eventTimestamp;

    protected TradingEvent(
            final Object source,
            final Order originalOrder,
            final EventType eventType,
            final LocalDateTime eventTimestamp) {
        super(source);
        this.originalOrder = originalOrder;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
    }
}
