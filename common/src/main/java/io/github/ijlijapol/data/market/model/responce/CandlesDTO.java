package io.github.ijlijapol.data.market.model.responce;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class CandlesDTO {

    private LocalDateTime startPeriodTime;

    private LocalDateTime endPeriodTime;

    private List<CandleDTO> candles;
}
