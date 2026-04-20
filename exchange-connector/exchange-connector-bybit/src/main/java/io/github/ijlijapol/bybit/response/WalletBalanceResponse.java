package io.github.ijlijapol.bybit.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Ответ на запрос getWalletBalance
 */
@Getter
@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletBalanceResponse {

    @JsonProperty("accountIMRate")
    private double accountIMRate;

    @JsonProperty("totalMaintenanceMarginByMp")
    private double totalMaintenanceMarginByMp;

    @JsonProperty("totalInitialMargin")
    private double totalInitialMargin;

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("accountMMRate")
    private double accountMMRate;

    @JsonProperty("accountMMRateByMp")
    private double accountMMRateByMp;

    @JsonProperty("accountIMRateByMp")
    private double accountIMRateByMp;

    @JsonProperty("totalInitialMarginByMp")
    private double totalInitialMarginByMp;

    @JsonProperty("totalMaintenanceMargin")
    private double totalMaintenanceMargin;

    @JsonProperty("totalEquity")
    private double totalEquity;

    @JsonProperty("totalMarginBalance")
    private double totalMarginBalance;

    @JsonProperty("totalAvailableBalance")
    private double totalAvailableBalance;

    @JsonProperty("totalPerpUPL")
    private double totalPerpUPL;

    @JsonProperty("totalWalletBalance")
    private double totalWalletBalance;

    @JsonProperty("accountLTV")
    private double accountLTV;

    @JsonProperty("coin")
    private List<CoinBalanceData> coin;
}