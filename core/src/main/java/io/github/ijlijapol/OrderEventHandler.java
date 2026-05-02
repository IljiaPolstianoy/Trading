/*
package io.github.ijlijapol.bybit;

import io.github.ijlijapol.bybit.exception.ByBitOrderSendingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventHandler {

    @EventListener
    public void handlerOrderCreatedEvent(final OrderCreatedEvent event) {
        log.info("📦 Обработка успешного ордера: orderId={}, symbol={}, side={}, qty={}",
                event.getOrderId(),
                event.getOriginalOrder().getSymbol(),
                event.getOriginalOrder().getSide(),
                event.getOriginalOrder().getAmount()
        );

        try {
            // Здесь будет сохранение в БД, обновление портфеля и т.д.
            // TODO: добавить бизнес-логику

            log.info("✅ Ордер успешно обработан: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ Ошибка при обработке успешного ордера: orderId={}, error={}",
                    event.getOrderId(),
                    e.getMessage(),
                    e
            );
        }
    }

    @EventListener
    public void handlerOrderFailedEvent(final OrderFailedEvent event) {
        String errorType = event.getErrorCode() != null ? "API ошибка" : "Сетевая ошибка";

        if (event.getErrorCode() != null) {
            log.trace("Начало обработки API-ошибки от Bybit");
        } else {
            log.error("❌ {}: message={}, order={}, side={}, qty={}, price={}",
                    errorType,
                    event.getErrorMessage(),
                    event.getOriginalOrder().getSymbol(),
                    event.getOriginalOrder().getSide(),
                    event.getOriginalOrder().getAmount(),
                    event.getOriginalOrder().getPrice()
            );
        }

        try {
            // Здесь будет уведомление и т.д.
            // TODO: добавить бизнес-логику

            log.debug("Обработка ошибочного ордера");
            throw new ByBitOrderSendingException("Ошибка отправки ордера на биржу ByBit");
        } catch (Exception e) {
            log.error("❌ Ошибка при обработке failed-события: {}", e.getMessage(), e);
        }
    }
}
*/
