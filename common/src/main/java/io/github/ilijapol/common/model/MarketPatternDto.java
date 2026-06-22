package io.github.ilijapol.common.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder(toBuilder = true)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MarketPatternDto {

    private CandlesDTO candlesDTO;

    private Side side;

    public void addCandle(final CandleDTO candleDTO) {
        candlesDTO.getCandles().add(candleDTO);
    }

    public int size() {
        return candlesDTO.getCandles().size();
    }
}