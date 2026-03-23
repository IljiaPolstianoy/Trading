package io.github.ijlijapol.response;

import io.github.ijlijapol.model.order.Order;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Событие, возникающее при ошибке создания/обработки ордера.
 * Поддерживает классификацию ошибок для разных типов обработки.
 */
@Getter
public class OrderFailedEvent extends TradingEvent {

    private final String errorMessage;
    private final Integer errorCode;
    private final ErrorCategory errorCategory;
    private final Throwable cause;

    private OrderFailedEvent(
            final Object source,
            final Order originalOrder,
            final LocalDateTime eventTimestamp,
            final String errorMessage,
            final Integer errorCode,
            final Throwable cause
    ) {
        super(source, originalOrder, EventType.ORDER_FAILED, eventTimestamp);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.errorCategory = ErrorCategory.fromApiErrorCode(errorCode);
        this.cause = cause;
    }


    /**
     * Конструктор для API-ошибок
     *
     * @param source         источник события
     * @param originalOrder  оригинальный ордер
     * @param eventTimestamp временная метка события
     * @param errorMessage   сообщение об ошибке
     * @param errorCode      код ошибки
     */
    public OrderFailedEvent(
            final Object source,
            final Order originalOrder,
            final LocalDateTime eventTimestamp,
            final String errorMessage,
            final Integer errorCode
    ) {

        this(
                source,
                originalOrder,
                eventTimestamp,
                errorMessage,
                errorCode,
                null
        );
    }

    /**
     * Конструктор для сетевых ошибок
     *
     * @param source        источник события
     * @param originalOrder оригинальный ордер
     * @param cause         ошибка
     */
    public OrderFailedEvent(
            final Object source,
            final Order originalOrder,
            final Throwable cause
    ) {
        this(
                source,
                originalOrder,
                LocalDateTime.now(),
                cause.getMessage(),
                null,
                cause
        );
    }

    /**
     * Конструктор для ошибки формата "Неизвестный формат ответа"
     *
     * @param source        источник события
     * @param originalOrder оригинальный ордер
     * @param errorMessage  сообщение об ошибке
     */
    public OrderFailedEvent(
            final Object source,
            final Order originalOrder,
            final String errorMessage
    ) {
        this(
                source,
                originalOrder,
                LocalDateTime.now(),
                errorMessage,
                null,
                null
        );
    }
}