package io.github.ijlijapol.bybit.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Данные по балансу конкретной монеты
 */
@Getter
@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinBalanceData {

    @JsonProperty("spotBorrow")
    private double spotBorrow;

    @JsonProperty("availableToBorrow")
    private String availableToBorrow;

    @JsonProperty("bonus")
    private double bonus;

    @JsonProperty("accruedInterest")
    private double accruedInterest;

    @JsonProperty("availableToWithdraw")
    private String availableToWithdraw;

    @JsonProperty("totalOrderIM")
    private double totalOrderIM;

    @JsonProperty("equity")
    private double equity;

    @JsonProperty("totalPositionMM")
    private double totalPositionMM;

    @JsonProperty("usdValue")
    private double usdValue;

    @JsonProperty("unrealisedPnl")
    private double unrealisedPnl;

    @JsonProperty("collateralSwitch")
    private boolean collateralSwitch;

    @JsonProperty("spotHedgingQty")
    private double spotHedgingQty;

    @JsonProperty("borrowAmount")
    private double borrowAmount;

    @JsonProperty("totalPositionIM")
    private double totalPositionIM;

    @JsonProperty("walletBalance")
    private double walletBalance;

    @JsonProperty("cumRealisedPnl")
    private double cumRealisedPnl;

    @JsonProperty("locked")
    private double locked;

    @JsonProperty("marginCollateral")
    private boolean marginCollateral;

    @JsonProperty("coin")
    private String coin;
}