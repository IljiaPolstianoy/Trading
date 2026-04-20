package io.github.ijlijapol.bybit;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.restApi.BybitApiAsyncTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import io.github.ijlijapol.bybit.model.Symbol;
import io.github.ijlijapol.bybit.model.order.ModifiedOrder;
import io.github.ijlijapol.bybit.model.order.Order;
import io.github.ijlijapol.bybit.model.order.TradeOrderType;
import io.github.ijlijapol.bybit.response.CreateOrderCallback;
import io.github.ijlijapol.contract.ExchangeConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public class ByBitExchangeConnectorImpl implements ExchangeConnector {

    private final BybitApiAsyncTradeRestClient client;
    private final ApplicationEventPublisher eventPublisher;

    public ByBitExchangeConnectorImpl(
            final String apiKey,
            final String apiSecret,
            final ApplicationEventPublisher eventPublisher
    ) {
        this.client = BybitApiClientFactory
                .newInstance(apiKey, apiSecret, BybitApiConfig.MAINNET_DOMAIN, true)
                .newAsyncTradeRestClient();
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void createNewOrder(final Order order) {
        log.info("🚀 Отправка ордера на Bybit: symbol={}, side={}, qty={}",
                order.getSymbol(),
                order.getSide(),
                order.getAmount()
        );

        final TradeOrderRequest byBitOrder = TradeOrderRequest.builder()
                .category(CategoryType.SPOT)
                .symbol(order.getSymbol().toString())
                .orderType(getOrderType(order.getOrderType()))
                .qty(order.getAmount().toString())
                .side(getSide(order.getSide()))
                .marketUnit("quoteCoin")
                .build();


        client.createOrder(byBitOrder, new CreateOrderCallback(eventPublisher, order));

        log.debug("Запрос отправлен: {}", byBitOrder);
    }

    @Override
    public boolean changeOldOrder(final ModifiedOrder modifiedOrder) {
        return false;
    }

    @Override
    public boolean cancelOrder(final String orderID) {
        return false;
    }

    @Override
    public boolean cancelAllOrders(final TradeOrderType orderType) {
        return false;
    }

    @Override
    public boolean cancelAllOrders(final Symbol symbol) {
        return false;
    }

    private com.bybit.api.client.domain.TradeOrderType getOrderType(final TradeOrderType orderType) {
        return switch (orderType) {
            case LIMIT -> com.bybit.api.client.domain.TradeOrderType.LIMIT;
            case MARKET -> com.bybit.api.client.domain.TradeOrderType.MARKET;
        };
    }

    private Side getSide(final io.github.ijlijapol.bybit.model.order.Side side) {
        return switch (side) {
            case BUY -> Side.BUY;
            case SELL -> Side.SELL;
        };
    }
}