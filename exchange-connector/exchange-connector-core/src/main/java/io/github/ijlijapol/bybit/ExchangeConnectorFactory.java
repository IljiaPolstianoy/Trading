package io.github.ijlijapol.bybit;

import io.github.ijlijapol.contract.ApiClientConnector;
import io.github.ijlijapol.contract.ExchangeConnector;
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
