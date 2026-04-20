package io.github.ijlijapol.bybit;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.account.AccountType;
import com.bybit.api.client.domain.asset.request.AssetDataRequest;
import com.bybit.api.client.restApi.BybitApiAssetRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import io.github.ijlijapol.bybit.mapper.MapperByBitWallet;
import io.github.ijlijapol.bybit.response.AssetBalanceResponse;
import io.github.ijlijapol.contract.ApiClientConnector;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class ApiByBitClientConnectorImpl implements ApiClientConnector {

    private final BybitApiAssetRestClient client;

    public ApiByBitClientConnectorImpl(
            final String apiKey,
            final String apiSecret
    ) {
        this.client = BybitApiClientFactory.newInstance(apiKey, apiSecret, BybitApiConfig.MAINNET_DOMAIN, true).newAssetRestClient();
    }

    @Override
    public BigDecimal getWalletActiveBalance(final String active) {
        log.info("Отправка на биржу ByBit запроса на получения количества coin={}", active);

        final AssetDataRequest request = AssetDataRequest.builder()
                .accountType(AccountType.UNIFIED)
                .coin(active)
                .build();

        final GenericResponse<AssetBalanceResponse> response = MapperByBitWallet
                .toAssetBalanceResponse(client.getAssetSingleCoinBalance(request));

        return response.getResult().getBalance().getWalletBalance();
    }
}
