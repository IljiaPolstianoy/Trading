package io.github.ijlijapol.bybit.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetBalanceResponse {

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("bizType")
    private Integer bizType;

    @JsonProperty("accountId")
    private Long accountId;

    @JsonProperty("memberId")
    private Long memberId;

    @JsonProperty("balance")
    private CoinAssetBalance balance;
}