package io.github.ilijapol.common.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder(toBuilder = true)
@Getter
@ToString
public class MarketPatternDto {

    @NotNull
    private CandlesDTO candlesDTO;

    @NotNull
    private Side side;
}