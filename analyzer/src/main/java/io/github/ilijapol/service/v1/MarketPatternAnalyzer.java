package io.github.ilijapol.service.v1;

import io.github.ilijapol.bybit.MarketDataFactory;
import io.github.ilijapol.common.contract.LoaderMarketData;
import io.github.ilijapol.common.model.*;
import io.github.ilijapol.service.MarketPatternService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class MarketPatternAnalyzer {

    private final LoaderMarketData loaderMarketData = MarketDataFactory.getByBitStockMarket();
    private final MarketPatternService patternService;

    public boolean analyzeMarketdata() {
        log.info("Начало анализа рыночных данных за последние пять лет");

        final CandlesDTO candlesDTO = getCandlesDTO();
        final List<List<Boolean>> potentialPatterns = getPotentialPatterns(candlesDTO.getCandles());
        final Map<List<Boolean>, Integer> sortedPotentialPatterns = sortPatterns(potentialPatterns);
        final MarketPatternDto pattern = getActualPattern(sortedPotentialPatterns);

        patternService.save(pattern);
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
                pattern.add(converterBooleanToDirection(iteratorCandleDTO.next().getDirection()));
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

    private MarketPatternDto getActualPattern(final Map<List<Boolean>, Integer> sortedPotentialPatterns) {
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

        final CandlesDTO candlesDTO = CandlesDTO.builder()
                .candles(new TreeSet<>())
                .build();

        log.info("Найден актуальный паттерн: {}", actualPattern);
        return MarketPatternDto.builder().candlesDTO(converterActualPatternToCandlesDTO(actualPattern)).build();
    }


    /**
     * Метод помогает преобразовать новую версию Candle в старый формат для сохранения совместимости кода
     *
     * @param directionCandle направление свечи (бычья или медвежья)
     * @return направление свечи в виде boolean (true для бычьей, false для медвежьей)
     */
    private boolean converterBooleanToDirection(final DirectionCandle directionCandle) {
        log.trace("Конвертация DirectionCandle в boolean. DirectionCandle: {}", directionCandle);
        if (directionCandle == DirectionCandle.BULLISH) {
            return true;
        } else if (directionCandle == DirectionCandle.BEARICH) {
            return false;
        } else {
            throw new IllegalArgumentException("Unknown direction candle: " + directionCandle);
        }
    }

    /**
     * Метод помогает преобразовать актуальный паттерн в формат CandlesDTO для сохранения совместимости кода.
     * Метод добавляет недостающие поля в новый CandlesDTO.
     * {@code Важно!} Метод добавляет ложные данные для совместимости.
     *
     * @param actualPattern Старый паттерн в виде boolean
     * @return {@link CandlesDTO} с заполненными(ложными данными)ормат: {Метод}{Версия}{Характеристика} полями для совместимости кода
     */
    private CandlesDTO converterActualPatternToCandlesDTO(final List<Boolean> actualPattern) {
        log.trace("Конвертация актуального паттерна в CandlesDTO. Актуальный паттерн: {}", actualPattern);

        TreeSet<CandleDTO> candles = new TreeSet<>();
        for (Boolean direction : actualPattern) {
            candles.add(CandleDTO.builder()
                    .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                    .maxPrice(BigDecimal.ZERO)
                    .minPrice(BigDecimal.ZERO)
                    .openPrice(BigDecimal.ZERO)
                    .closePrice(BigDecimal.ZERO)
                    .volume(BigDecimal.ZERO)
                    .startTime(LocalDateTime.now())
                    .direction(direction ? DirectionCandle.BULLISH : DirectionCandle.BEARICH)
                    .build());
        }

        return CandlesDTO.builder()
                .startPeriodTime(LocalDateTime.now())
                .endPeriodTime(LocalDateTime.now())
                .candles(candles)
                .build();
    }
}