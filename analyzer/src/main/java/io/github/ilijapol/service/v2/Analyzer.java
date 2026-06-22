package io.github.ilijapol.service.v2;

import io.github.ilijapol.bybit.MarketDataFactory;
import io.github.ilijapol.common.contract.LoaderMarketData;
import io.github.ilijapol.common.model.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class Analyzer {
    private final LoaderMarketData loaderMarketData;

    public Analyzer() {
        this.loaderMarketData = MarketDataFactory.getByBitStockMarket();
        log.debug("Analyzer initialized with ByBitStockMarket LoaderMarketData implementation");
    }

    public List<MarketPatternDto> analysis(
            final LastTime period,
            final TimeFrame timeFrame
    ) {
        final CandlesDTO candlesDTO = getCandlesDTO(period, timeFrame);
        final HashMap<MarketPatternDto, Integer> potentialPattern = searchPotentialPattern(candlesDTO);
        final List<MarketPatternDto> marketPatternsDTO = searchTopPattern(potentialPattern);
        log.debug("Анализ завершен. Найдено {} потенциальных паттернов", marketPatternsDTO.size());
        return marketPatternsDTO;
    }

    private CandlesDTO getCandlesDTO(
            final LastTime period,
            final TimeFrame timeFrame
    ) {
        log.trace("Получение рыночных данных за период: {} и таймфрейм: {}", period, timeFrame);

        final RecentMarketDataRequest request = RecentMarketDataRequest.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(timeFrame)
                .lastTime(period)
                .build();
        return loaderMarketData.loadRecentMarketData(request);
    }

    private HashMap<MarketPatternDto, Integer> searchPotentialPattern(final CandlesDTO candlesDTO) {
        log.debug("Поиск потенциальных паттернов из Candles. Size candles: {}", candlesDTO.getCandles().size());

        final List<CandleDTO> candles = new ArrayList<>(candlesDTO.getCandles());
        final HashMap<MarketPatternDto, Integer> potentialPattern = new HashMap<>();
        final int minCandles = 5;
        final int maxCandles = 15;

        for (int offset = 0; offset <= candles.size() - minCandles - 1; offset++) {
            final MarketPatternDto marketPatternDto = new MarketPatternDto();
            int targetOffset = offset + maxCandles;

            for (int i = offset; i < targetOffset && i <= candles.size(); i++) {
                final CandleDTO candleDTO = candles.get(i);

                if (checkDojiCandle(candleDTO)) {
                    targetOffset++;
                    continue;
                }

                marketPatternDto.addCandle(candleDTO);
                isValidForAddition(marketPatternDto, potentialPattern, candles.get(i + 1));
            }
        }

        return potentialPattern;
    }

    private void isValidForAddition(
            final MarketPatternDto marketPatternDto,
            final HashMap<MarketPatternDto, Integer> potentialPattern,
            final CandleDTO candleDTO
    ) {
        log.trace("Проверка валидности паттерна для добавления в потенциальные паттерны.");

        if (marketPatternDto.size() >= 5) {
            marketPatternDto.setSide(getOrderSide(candleDTO));

            if (potentialPattern.containsKey(marketPatternDto)) {
                potentialPattern.put(marketPatternDto, potentialPattern.get(marketPatternDto) + 1);
            } else {
                potentialPattern.put(marketPatternDto, 1);
            }
        }
    }

    private Side getOrderSide(final CandleDTO candleDTO) {
        log.trace("Определение стороны ордера для свечи: {}", candleDTO);

        if (candleDTO.getOpenPrice().compareTo(candleDTO.getClosePrice()) < 0) {
            return Side.BUY;
        } else {
            return Side.SELL;
        }
    }

    private List<MarketPatternDto> searchTopPattern(final HashMap<MarketPatternDto, Integer> potentialPattern) {
        log.trace("Поиск топ паттернов из потенциальных паттернов.Size potentialPattern: {}", potentialPattern.size());

        final HashMap<MarketPatternDto, Integer> topPatterns = new HashMap<>();

        for (MarketPatternDto marketPattern : potentialPattern.keySet()) {
            if (topPatterns.isEmpty()) {
                topPatterns.put(marketPattern, potentialPattern.get(marketPattern));
                continue;
            }

            final Integer currentMax = topPatterns.values().stream().max(Integer::compareTo).get();

            if (potentialPattern.get(marketPattern) > currentMax) {
                isValidForRemove(topPatterns);
                topPatterns.put(marketPattern, potentialPattern.get(marketPattern));
            }
        }

        return new ArrayList<>(topPatterns.keySet());
    }

    private void isValidForRemove(final HashMap<MarketPatternDto, Integer> topPatterns) {
        log.trace("Проверка необходимости удаления паттерна из топ 3. Текущие паттерны: {}", topPatterns);

        if (topPatterns.size() == 3) {
            final Integer currentMin = topPatterns.values().stream().min(Integer::compareTo).get();

            for (MarketPatternDto marketPatternCheckMin : topPatterns.keySet()) {
                if (topPatterns.get(marketPatternCheckMin).equals(currentMin)) {
                    topPatterns.remove(marketPatternCheckMin);
                    return;
                }
            }
        }
    }

    /**
     * Метод проверят, является ли свеча Doji свечой используя правило -
     * свеча является doji свечой, если ее тело меньше или равно 5% от теней
     *
     * @param candleDTO проверяемая свеча
     * @return Возвращается {@code true} если свеча является doji свечой и {@code false} если нет
     */
    private boolean checkDojiCandle(final CandleDTO candleDTO) {
        log.trace("Проверка свечи на Doji: {}", candleDTO);
        final BigDecimal openCloseSpread = candleDTO.getOpenPrice().subtract(candleDTO.getClosePrice()).abs();
        final BigDecimal highLowSpread = candleDTO.getMaxPrice().subtract(candleDTO.getMinPrice()).abs();
        return openCloseSpread.compareTo(highLowSpread.multiply(BigDecimal.valueOf(0.05))) <= 0;
    }
}
