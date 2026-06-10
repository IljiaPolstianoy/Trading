package io.github.ilijapol.dto;

import io.github.ilijapol.common.model.Side;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class MarketPatternDto {

    @NotNull
    private Set<CandleDto> candleDirections;

    @NotNull
    private Side side;
}