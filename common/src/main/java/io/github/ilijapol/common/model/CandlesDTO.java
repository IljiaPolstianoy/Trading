package io.github.ilijapol.common.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.TreeSet;

@Builder(toBuilder = true)
@Getter
@ToString
public class CandlesDTO {

    @NonNull
    private LocalDateTime startPeriodTime;

    @NonNull
    private LocalDateTime endPeriodTime;

    @NonNull
    private TreeSet<CandleDTO> candles;
}
