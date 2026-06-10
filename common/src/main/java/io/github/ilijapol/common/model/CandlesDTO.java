package io.github.ilijapol.common.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

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
