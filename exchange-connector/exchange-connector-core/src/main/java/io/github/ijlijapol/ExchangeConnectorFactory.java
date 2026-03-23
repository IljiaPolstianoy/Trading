package io.github.ijlijapol;

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
}
