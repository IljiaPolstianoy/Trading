package io.github.ijlijapol.bybit.response;

import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.bybit.api.client.restApi.BybitApiCallback;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.ijlijapol.bybit.model.order.Order;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
public class CreateOrderCallback implements BybitApiCallback<Object> {

    private final ApplicationEventPublisher eventPublisher;
    private final Order originalOrder;

    @Override
    public void onResponse(Object response) {

        final GenericResponse<OrderResponse> genericResponse;
        final Optional<GenericResponse<OrderResponse>> genericResponseOptional = convertedResponse(response);

        if (genericResponseOptional.isEmpty()) {
            return;
        } else {
            genericResponse = genericResponseOptional.get();
        }

        log.debug("Получен ответ от Bybit: retCode={}, retMsg={}, time={}",
                genericResponse.getRetCode(),
                genericResponse.getRetMsg(),
                genericResponse.getTime());

        if (genericResponse.getRetCode() == 0) {
            final OrderResponse orderResponse = genericResponse.getResult();

            log.info("✅ Ордер успешно отправлен: orderId={}, symbol={}, side={}, qty={}",
                    genericResponse.getResult().getOrderId(),
                    originalOrder.getSymbol(),
                    originalOrder.getSide(),
                    originalOrder.getAmount()
            );

            eventPublisher.publishEvent(new OrderCreatedEvent(
                    this,
                    originalOrder,
                    orderResponse,
                            Instant.ofEpochMilli(genericResponse.getTime()).atOffset(ZoneOffset.UTC).toLocalDateTime()
                    )
            );
        } else {
            log.error("❌ API ошибка Bybit: code={}, message={}, symbol={}, side={}, qty={}",
                    genericResponse.getRetCode(),
                    genericResponse.getRetMsg(),
                    originalOrder.getSymbol(),
                    originalOrder.getSide(),
                    originalOrder.getAmount()
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
        log.error("❌ Сетевая ошибка при отправке ордера: symbol={}, side={}, qty={}, error={}",
                originalOrder.getSymbol(),
                originalOrder.getSide(),
                originalOrder.getAmount(),
                cause.getMessage(),
                cause
        );

        eventPublisher.publishEvent(new OrderFailedEvent(
                this,
                originalOrder,
                cause
        ));
    }

    /**
     * Конвертирует сырой ответ от Bybit в GenericResponse<OrderResponse>.
     * При ошибке публикует событие OrderFailedEvent и возвращает null.
     *
     * @param response сырой ответ от API
     * @return сконвертированный ответ или null в случае ошибки
     */
    private Optional<GenericResponse<OrderResponse>> convertedResponse(final Object response) {
        final ObjectMapper objectMapper = new ObjectMapper();
        log.trace("Начало конвертации ответа от bybit {} в класс GenericResponse<OrderResponse>", response);
        try {
            final JavaType type = TypeFactory.defaultInstance()
                    .constructParametricType(GenericResponse.class, OrderResponse.class);

            return Optional.of(objectMapper.convertValue(response, type));
        } catch (Exception e) {
            final String responseType = response != null ? response.getClass().getSimpleName() : "null";
            log.error("❌ Неожиданный формат ответа от Bybit: {}, ожидался GenericResponse. symbol={}",
                    responseType, originalOrder.getSymbol());
            eventPublisher.publishEvent(new OrderFailedEvent(
                            this,
                            originalOrder,
                            "Неожиданный формат ответа: " + responseType
                    )
            );
            return Optional.empty();
        }
    }
}
