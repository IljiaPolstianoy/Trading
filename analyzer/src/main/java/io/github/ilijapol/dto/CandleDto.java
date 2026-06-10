package io.github.ilijapol.dto;

import io.github.ilijapol.common.model.DirectionCandle;
import io.github.ilijapol.common.model.TimeFrame;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
public class CandleDto {

    @NotNull
    private TimeFrame timeFrame;

    @NotNull
    private BigDecimal maxPrice;

    @NotNull
    private BigDecimal minPrice;

    @NotNull
    private BigDecimal openPrice;

    @NotNull
    private BigDecimal closePrice;

    @NotNull
    private BigDecimal volume;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private DirectionCandle direction;
}