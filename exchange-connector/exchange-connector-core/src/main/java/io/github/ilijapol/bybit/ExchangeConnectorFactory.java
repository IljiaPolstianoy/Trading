package io.github.ilijapol.bybit;

import io.github.ilijapol.bybit.connector.ApiByBitClientConnectorImpl;
import io.github.ilijapol.bybit.connector.ByBitExchangeConnectorImpl;
import io.github.ilijapol.common.contract.ApiClientConnector;
import io.github.ilijapol.common.contract.ExchangeConnector;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeConnectorFactory {

    private final ApplicationEventPublisher eventPublisher;

    public ExchangeConnector getExchangeConnectorByBit(final String apiKey, final String apiSecret) {
        return new ByBitExchangeConnectorImpl(apiKey, apiSecret, eventPublisher);
    }

    public ApiClientConnector getApiClientConnectorByBit(final String apiKey, final String apiSecret) {
        return new ApiByBitClientConnectorImpl(apiKey, apiSecret);
    }
}
