package io.github.ijlijapol.model.responce;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

@Data
@Builder(toBuilder = true)
public class CandlesDTO {

    private LocalDateTime startPeriodTime;

    private LocalDateTime endPeriodTime;

    private TreeSet<CandleDTO> candles;
}
