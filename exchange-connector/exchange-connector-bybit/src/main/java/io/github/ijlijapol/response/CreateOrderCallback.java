package io.github.ijlijapol.response;

import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.bybit.api.client.restApi.BybitApiCallback;
import io.github.ijlijapol.model.order.Order;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.ZoneOffset;

@AllArgsConstructor
@Slf4j
public class CreateOrderCallback implements BybitApiCallback<Object> {

    private final ApplicationEventPublisher eventPublisher;
    private final Order originalOrder;

    @Override
    public void onResponse(Object response) {


        if (!(response instanceof GenericResponse<?>)) {
            String responseType = response != null ? response.getClass().getSimpleName() : "null";
            log.error("❌ Неожиданный формат ответа от Bybit: {}, ожидался GenericResponse. symbol={}",
                    responseType, originalOrder.getSymbol());
            eventPublisher.publishEvent(new OrderFailedEvent(
                            this,
                            originalOrder,
                            "Неожиданный формат ответа: " + responseType
                    )
            );
            return;
        }

        @SuppressWarnings("unchecked")
        final GenericResponse<OrderResponse> genericResponse = (GenericResponse<OrderResponse>) response;

        log.debug("Получен ответ от Bybit: retCode={}, retMsg={}, time={}",
                genericResponse.getRetCode(),
                genericResponse.getRetMsg(),
                genericResponse.getTime());

        if (genericResponse.getRetCode() == 0) {
            log.info("✅ Ордер успешно отправлен: orderId={}, symbol={}, side={}, qty={}, price={}",
                    genericResponse.getResult().getOrderId(),
                    originalOrder.getSymbol(),
                    originalOrder.getSide(),
                    originalOrder.getAmount(),
                    originalOrder.getPrice()
            );

            eventPublisher.publishEvent(new OrderCreatedEvent(
                            this,
                            originalOrder,
                            genericResponse.getResult(),
                            Instant.ofEpochMilli(genericResponse.getTime()).atOffset(ZoneOffset.UTC).toLocalDateTime()
                    )
            );
        } else {
            log.error("❌ API ошибка Bybit: code={}, message={}, symbol={}, side={}, qty={}, price={}",
                    genericResponse.getRetCode(),
                    genericResponse.getRetMsg(),
                    originalOrder.getSymbol(),
                    originalOrder.getSide(),
                    originalOrder.getAmount(),
                    originalOrder.getPrice()
            );

            eventPublisher.publishEvent(new OrderFailedEvent(
                    this,
                    originalOrder,
                    Instant.ofEpochMilli(genericResponse.getTime()).atOffset(ZoneOffset.UTC).toLocalDateTime(),
                    genericResponse.getRetMsg(),
                    genericResponse.getRetCode()
            ));
        }
    }

    @Override
    public void onFailure(Throwable cause) {
        log.error("❌ Сетевая ошибка при отправке ордера: symbol={}, side={}, qty={}, price={}, error={}",
                originalOrder.getSymbol(),
                originalOrder.getSide(),
                originalOrder.getAmount(),
                originalOrder.getPrice(),
                cause.getMessage(),
                cause
        );

        eventPublisher.publishEvent(new OrderFailedEvent(
                this,
                originalOrder,
                cause
        ));
    }
}
