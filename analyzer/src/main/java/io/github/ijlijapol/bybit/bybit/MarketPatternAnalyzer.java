package io.github.ijlijapol.bybit.bybit;

import io.github.ijlijapol.bybit.MarketDataFactory;
import io.github.ijlijapol.bybit.model.Symbol;
import io.github.ijlijapol.bybit.model.request.LastTime;
import io.github.ijlijapol.bybit.model.request.RecentMarketDataRequest;
import io.github.ijlijapol.bybit.model.request.TimeFrame;
import io.github.ijlijapol.bybit.model.responce.CandleDTO;
import io.github.ijlijapol.bybit.model.responce.CandlesDTO;
import io.github.ijlijapol.contract.LoaderMarketData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class MarketPatternAnalyzer {

    private final LoaderMarketData loaderMarketData = MarketDataFactory.getByBitStockMarket();
    private final PatternRepository patternRepository;

    public boolean analyzeMarketdata() {
        log.info("Начало анализа рыночных данных за последние пять лет");

        final CandlesDTO candlesDTO = getCandlesDTO();
        final List<List<Boolean>> potentialPatterns = getPotentialPatterns(candlesDTO.getCandles());
        final Map<List<Boolean>, Integer> sortedPotentialPatterns = sortPatterns(potentialPatterns);
        final Pattern pattern = getActualPattern(sortedPotentialPatterns);

        patternRepository.save(pattern);
        return true;
    }

    private CandlesDTO getCandlesDTO() {
        log.debug("Получение рыночных данных за последние пять лет.");

        return loaderMarketData.loadRecentMarketData(
                RecentMarketDataRequest.builder()
                        .symbol(Symbol.BTCUSDT)
                        .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                        .lastTime(LastTime.DAY)
                        .build()
        );
    }

    private List<List<Boolean>> getPotentialPatterns(TreeSet<CandleDTO> candleDTOSet) {
        log.debug("Поиск потенциальных паттернов.");

        List<List<Boolean>> potentialPatterns = new ArrayList<>();

        while (!candleDTOSet.isEmpty()) {
            Optional<List<Boolean>> patternOptional = getPotentialPattern(candleDTOSet);
            if (patternOptional.isPresent()) {
                potentialPatterns.add(patternOptional.get());
            } else {
                break;
            }
            candleDTOSet.pollFirst();
        }

        log.trace("Найдены потенциальные паттерны: {}", potentialPatterns);
        return potentialPatterns;
    }

    private Optional<List<Boolean>> getPotentialPattern(final TreeSet<CandleDTO> candleDTOSet) {
        log.trace("Получение потенциального паттерна.");

        Iterator<CandleDTO> iteratorCandleDTO = candleDTOSet.iterator();
        List<Boolean> pattern = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            if (iteratorCandleDTO.hasNext()) {
                pattern.add(iteratorCandleDTO.next().isGrowing());
            } else {
                return Optional.empty();
            }
        }

        log.trace("Найден потенциальный паттерн: {}", pattern);
        return Optional.of(pattern);
    }

    private Map<List<Boolean>, Integer> sortPatterns(final List<List<Boolean>> potentialPatterns) {
        log.trace("Начало сортировки потенциальных паттернов.");

        Map<List<Boolean>, Integer> sortedPotentialPatterns = new HashMap<>();

        for (List<Boolean> potentialPattern : potentialPatterns) {
            if (sortedPotentialPatterns.containsKey(potentialPattern)) {
                sortedPotentialPatterns.put(potentialPattern, sortedPotentialPatterns.get(potentialPattern) + 1);
            } else {
                sortedPotentialPatterns.put(potentialPattern, 1);
            }
        }
        return sortedPotentialPatterns;
    }

    private Pattern getActualPattern(final Map<List<Boolean>, Integer> sortedPotentialPatterns) {
        log.debug("Получение актуального паттерна.");

        List<Boolean> actualPattern = new ArrayList<>();

        for (List<Boolean> potentialPattern : sortedPotentialPatterns.keySet()) {
            if (actualPattern.isEmpty()) {
                actualPattern = potentialPattern;
            } else {
                if (sortedPotentialPatterns.get(actualPattern) < sortedPotentialPatterns.get(potentialPattern)) {
                    actualPattern = potentialPattern;
                }
            }
        }

        log.info("Найден актуальный паттерн: {}", actualPattern);
        return Pattern.builder().candleDirections(actualPattern).build();
    }
}