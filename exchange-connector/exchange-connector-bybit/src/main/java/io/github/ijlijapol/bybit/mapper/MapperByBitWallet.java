package io.github.ijlijapol.bybit.mapper;

import com.bybit.api.client.domain.GenericResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.ijlijapol.bybit.response.AssetBalanceResponse;
import io.github.ijlijapol.bybit.response.WalletBalanceResult;

public class MapperByBitWallet {

    public static GenericResponse<WalletBalanceResult> toWalletBalanceResult(final Object response) {
        final ObjectMapper mapper = new ObjectMapper();
        final JavaType responseType = TypeFactory.defaultInstance().constructParametricType(
                GenericResponse.class,
                WalletBalanceResult.class
        );

        return mapper.convertValue(response, responseType);
    }

    public static GenericResponse<AssetBalanceResponse> toAssetBalanceResponse(final Object response) {
        final ObjectMapper mapper = new ObjectMapper();
        final JavaType responseType = TypeFactory.defaultInstance().constructParametricType(
                GenericResponse.class,
                AssetBalanceResponse.class);

        return mapper.convertValue(response, responseType);
    }
}
