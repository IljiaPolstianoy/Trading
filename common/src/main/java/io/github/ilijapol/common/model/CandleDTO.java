package io.github.ilijapol.common.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class CandleDTO implements Comparable<CandleDTO> {

    @NonNull
    private TimeFrame timeFrame;

    @NonNull
    private BigDecimal maxPrice;

    @NonNull
    private BigDecimal minPrice;

    @NonNull
    private BigDecimal openPrice;

    @NonNull
    private BigDecimal closePrice;

    @NonNull
    private BigDecimal volume;

    @NonNull
    private LocalDateTime startTime;

    @NonNull
    private DirectionCandle direction;

    @Override
    public int compareTo(CandleDTO o) {
        if (o == null || o.getStartTime() == null) {
            throw new NullPointerException("CandleDTO or startTime is null");
        }

        return this.getStartTime().compareTo(o.getStartTime());
    }
}
