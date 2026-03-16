package io.github.ijlijapol.model.responce;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class CandleDTO {

    private BigDecimal openPrice;

    private BigDecimal closePrice;

    private LocalDateTime startTime;
}
