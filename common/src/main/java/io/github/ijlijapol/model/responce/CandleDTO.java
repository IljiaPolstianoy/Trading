package io.github.ijlijapol.model.responce;

import io.github.ijlijapol.model.request.TimeFrame;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class CandleDTO implements Comparable<CandleDTO> {
    private BigDecimal openPrice;

    private BigDecimal closePrice;

    private LocalDateTime startTime;

    private boolean growing;

    public TimeFrame timeFrame;

    @Override
    public int compareTo(CandleDTO o) {
        if (o == null || o.getStartTime() == null) {
            throw new NullPointerException("CandleDTO or startTime is null");
        }

        return this.getStartTime().compareTo(o.getStartTime());
    }
}
