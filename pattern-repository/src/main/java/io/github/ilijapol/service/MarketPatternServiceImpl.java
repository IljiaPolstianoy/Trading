package io.github.ilijapol.service;

import io.github.ilijapol.MarketPatternRepository;
import io.github.ilijapol.common.model.CandlesDTO;
import io.github.ilijapol.common.model.MarketPatternDto;
import io.github.ilijapol.entity.Candle;
import io.github.ilijapol.entity.MarketPattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.TreeSet;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketPatternServiceImpl implements MarketPatternService {

    private final MarketPatternRepository patternRepository;

    @Override
    public MarketPattern save(final MarketPatternDto marketPatternDto) {
        log.info("Saving market pattern: {}", marketPatternDto);
        final MarketPattern marketPattern = MarketPattern.builder()
                .candles(converterDTOtoEntity(marketPatternDto.getCandlesDTO()))
                .side(marketPatternDto.getSide())
                .build();
        return patternRepository.save(marketPattern);
    }

    @Override
    public void delete(final Integer id) {
        log.info("Deleting market pattern: {}", id);
        patternRepository.deleteById(id);
    }

    private TreeSet<Candle> converterDTOtoEntity(final CandlesDTO candlesDTO) {
        log.trace("Converting CandlesDTO to TreeSet<Candle> for MarketPattern. CandlesDTO: {}", candlesDTO);
        return new TreeSet<>(candlesDTO.getCandles().stream()
                .map(candleDTO -> Candle.builder()
                        .timeFrame(candleDTO.getTimeFrame())
                        .maxPrice(candleDTO.getMaxPrice())
                        .minPrice(candleDTO.getMinPrice())
                        .openPrice(candleDTO.getOpenPrice())
                        .closePrice(candleDTO.getClosePrice())
                        .volume(candleDTO.getVolume())
                        .startTime(candleDTO.getStartTime())
                        .direction(candleDTO.getDirection())
                        .build())
                .toList());
    }
}
