package io.github.ijlijapol.bybit.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinAssetBalance {

    @JsonProperty("coin")
    private String coin;

    @JsonProperty("walletBalance")
    private BigDecimal walletBalance;

    @JsonProperty("transferBalance")
    private BigDecimal transferBalance;

    @JsonProperty("bonus")
    private BigDecimal bonus;

    @JsonProperty("transferSafeAmount")
    private String transferSafeAmount;

    @JsonProperty("ltvTransferSafeAmount")
    private String ltvTransferSafeAmount;
}