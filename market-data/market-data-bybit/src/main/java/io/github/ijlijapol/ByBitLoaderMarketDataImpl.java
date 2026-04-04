package io.github.ijlijapol;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.response.kline.MarketKlineEntry;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import io.github.ijlijapol.contract.LoaderMarketData;
import io.github.ijlijapol.exception.ByBitException;
import io.github.ijlijapol.exception.UncorrectedRequestByBit;
import io.github.ijlijapol.mapper.MapperByBitData;
import io.github.ijlijapol.mapper.MapperTimeFrame;
import io.github.ijlijapol.model.request.LastCandleRequest;
import io.github.ijlijapol.model.request.LastTime;
import io.github.ijlijapol.model.request.MarketDataForPeriodBetweenRequest;
import io.github.ijlijapol.model.request.RecentMarketDataRequest;
import io.github.ijlijapol.model.responce.CandleDTO;
import io.github.ijlijapol.model.responce.CandlesDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TreeSet;

@Slf4j
public class ByBitLoaderMarketDataImpl implements LoaderMarketData {

    private final BybitApiMarketRestClient client;

    public ByBitLoaderMarketDataImpl() {
        this.client = BybitApiClientFactory.newInstance().newMarketDataRestClient();
    }

    @Override
    public CandlesDTO loadRecentMarketData(final RecentMarketDataRequest recentMarketData) {
        if (recentMarketData == null) {
            log.error("recentMarketData is null");
            throw new UncorrectedRequestByBit("Объект RecentMarketData равен null");
        }
        if (recentMarketData.getSymbol() == null) {
            log.error("recentMarketData.symbol is null");
            throw new UncorrectedRequestByBit("Symbol в recentMarketData не может быть null");
        }
        if (recentMarketData.getLastTime() == null) {
            log.error("recentMarketData.lastTime is null");
            throw new UncorrectedRequestByBit("LastTime в recentMarketData не может быть null");
        }
        if (recentMarketData.getTimeFrame() == null) {
            log.error("recentMarketData.timeFrame is null");
            throw new UncorrectedRequestByBit("TimeFrame В recentMarketData не может быть null");
        }

        log.info("Загрузка последних рыночных свеч: symbol-{}, timeFrame={}, lastTime={}",
                recentMarketData.getSymbol(), recentMarketData.getTimeFrame(), recentMarketData.getLastTime());

        final Long startPeriod = getStartTimeFromLastTime(recentMarketData.getLastTime());
        final Long endDate = LocalDateTime.now(ZoneId.of("UTC")).toInstant(ZoneOffset.UTC).toEpochMilli();
        final MarketInterval marketInterval = MapperTimeFrame.toMarketInterval(recentMarketData.getTimeFrame());
        final com.bybit.api.client.domain.market.request.MarketDataRequest byBitRequest
                = com.bybit.api.client.domain.market.request.MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol(recentMarketData.getSymbol().toString())
                .marketInterval(marketInterval)
                .start(startPeriod)
                .end(endDate)
                .limit(1000)
                .build();

        log.debug("Первый запрос ByBit: start={}, end={}, limit=1000", startPeriod, endDate);
        final List<MarketKlineEntry> marketKlineEntries = MapperByBitData.convertFromResponse(sendRequest(byBitRequest));
        log.debug("Получено {} записей от ByBit", marketKlineEntries.size());
        final TreeSet<CandleDTO> candleDTOList = new TreeSet<>(MapperByBitData.convertFromMarketKlineEntry(marketKlineEntries));

        while (marketKlineEntries.size() == 1000) {

            marketKlineEntries.clear();

            final Long markerInteralLong = getTimeInMillisFromMarketInterval(marketInterval);
            final Long newStartTime = candleDTOList.getLast().getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli() + markerInteralLong;
            log.debug("Получено 1000 записей, запрашиваем следующую порцию начиная с newStartTime={}", newStartTime);
            byBitRequest.setStartTime(newStartTime);

            marketKlineEntries.addAll(MapperByBitData.convertFromResponse(sendRequest(byBitRequest)));
            candleDTOList.addAll(MapperByBitData.convertFromMarketKlineEntry(marketKlineEntries));
            log.debug("Добавлено еще {} записей всего сейчас {}", marketKlineEntries.size(), candleDTOList.size());
        }

        log.info("Загружено всего {} свечей за период с {} по {}", candleDTOList.size(), startPeriod, endDate);
        return CandlesDTO.builder()
                .startPeriodTime(Instant.ofEpochMilli(startPeriod).atZone(ZoneId.of("UTC")).toLocalDateTime())
                .endPeriodTime(LocalDateTime.now(ZoneOffset.UTC))
                .candles(candleDTOList)
                .build();
    }

    @Override
    public CandlesDTO loadMarketDateForPeriodBetween(final MarketDataForPeriodBetweenRequest marketDataForPeriodBetween) {
        if (marketDataForPeriodBetween == null) {
            log.error("marketDataForPeriodBetween is null");
            throw new UncorrectedRequestByBit("Объект marketDataForPeriodBetween равен null");
        }
        if (marketDataForPeriodBetween.getSymbol() == null) {
            log.error("marketDataForPeriodBetween.symbol is null");
            throw new UncorrectedRequestByBit("Symbol в MarketDataForPeriodBetween не может быть null");
        }
        if (marketDataForPeriodBetween.getTimeFrame() == null) {
            log.error("marketDataForPeriodBetween.timeFrame is null");
            throw new UncorrectedRequestByBit("TimeFrame in marketDataForPeriodBetween не может быть null");
        }
        if (marketDataForPeriodBetween.getStartTime() == null) {
            log.error("marketDataForPeriodBetween.startTime is null");
            throw new UncorrectedRequestByBit("StartTime in marketDataForPeriodBetween не может быть null");
        }
        if(marketDataForPeriodBetween.getEndTime() == null) {
            log.error("marketDataForPeriodBetween.endTime is null");
            throw new UncorrectedRequestByBit("EndTime in marketDataForPeriodBetween не может быть null");
        }

        log.info("Загрузка р=рыночных свечей за период: symbol={}, timeFrame={}, start={}, end={}",
                marketDataForPeriodBetween.getSymbol(),
                marketDataForPeriodBetween.getTimeFrame(),
                marketDataForPeriodBetween.getStartTime(),
                marketDataForPeriodBetween.getEndTime()
        );

        final Long startPeriod = marketDataForPeriodBetween.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
        final Long endDate = marketDataForPeriodBetween.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli();
        final MarketInterval marketInterval = MapperTimeFrame.toMarketInterval(marketDataForPeriodBetween.getTimeFrame());
        final com.bybit.api.client.domain.market.request.MarketDataRequest byBitRequest
                = com.bybit.api.client.domain.market.request.MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol(marketDataForPeriodBetween.getSymbol().toString())
                .marketInterval(marketInterval)
                .start(startPeriod)
                .end(endDate)
                .limit(1000)
                .build();

        log.debug("Первый запрос ByBit: start={}, end={}, limit=1000", startPeriod, endDate);
        final List<MarketKlineEntry> marketKlineEntries = MapperByBitData.convertFromResponse(sendRequest(byBitRequest));
        log.debug("Получено {} записей от ByBit", marketKlineEntries.size());
        final TreeSet<CandleDTO> candleDTOList = new TreeSet<>(MapperByBitData.convertFromMarketKlineEntry(marketKlineEntries));

        while (marketKlineEntries.size() == 1000) {

            marketKlineEntries.clear();

            final Long markerInteralLong = getTimeInMillisFromMarketInterval(marketInterval);
            final Long newStartTime = candleDTOList.getLast().getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli() + markerInteralLong;
            log.debug("Получено 1000 записей, запрашиваем следующую порцию начиная с newStartTime={}", newStartTime);
            byBitRequest.setStartTime(newStartTime);

            marketKlineEntries.addAll(MapperByBitData.convertFromResponse(sendRequest(byBitRequest)));
            candleDTOList.addAll(MapperByBitData.convertFromMarketKlineEntry(marketKlineEntries));
            log.debug("Добавлено еще {} записей всего сейчас {}", marketKlineEntries.size(), candleDTOList.size());
        }

        log.info("Загружено всего {} свечей за период с {} по {}", candleDTOList.size(), startPeriod, endDate);
        return CandlesDTO.builder()
                .startPeriodTime(marketDataForPeriodBetween.getStartTime())
                .endPeriodTime(marketDataForPeriodBetween.getEndTime())
                .candles(candleDTOList)
                .build();
    }

    @Override
    public CandleDTO loadLatestCandle(final LastCandleRequest marketDataRequest) {
        if (marketDataRequest  == null) {
            log.error("marketDataRequest is null");
            throw new UncorrectedRequestByBit("Объект marketDataRequest равен Null");
        }
        if (marketDataRequest.getTimeFrame() == null) {
            log.error("marketDataRequest.timeFrame is null");
            throw new UncorrectedRequestByBit("TimeFrame in marketDataRequest не может быть null");
        }
        if (marketDataRequest.getSymbol() == null) {
            log.error("marketDataRequest.symbol is null");
            throw new UncorrectedRequestByBit("Symbol in marketDataRequest не может быть null");
        }

        log.info("Загрузка последней свечи: symbol={}, timeFrame={}",
                marketDataRequest.getSymbol(),
                marketDataRequest.getTimeFrame()
        );

        final MarketInterval marketInterval = MapperTimeFrame.toMarketInterval(marketDataRequest.getTimeFrame());
        final Long endDate = LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC).toEpochMilli();
        final Long startTime = endDate - (getTimeInMillisFromMarketInterval(marketInterval));
        final com.bybit.api.client.domain.market.request.MarketDataRequest byBitRequest
                = com.bybit.api.client.domain.market.request.MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol(marketDataRequest.getSymbol().toString())
                .marketInterval(marketInterval)
                .start(startTime)
                .end(endDate)
                .limit(1)
                .build();

        log.debug("Запрос последней свечи: start={}, end={}, limit=1", startTime, endDate);
        final List<MarketKlineEntry> marketKlineEntry = MapperByBitData.convertFromResponse(sendRequest(byBitRequest));
        TreeSet<CandleDTO> candleDTOList = MapperByBitData.convertFromMarketKlineEntry(marketKlineEntry);
        final CandleDTO candleDTO = candleDTOList.getFirst();
        log.debug("Получена свеча: open={}, close={}", candleDTO.getOpenPrice(), candleDTO.getClosePrice());
        return candleDTO;
    }

    private Long getStartTimeFromLastTime(final LastTime lastTime) {
        final Long startTime = switch (lastTime) {
            case DAY -> LocalDateTime.now(ZoneId.of("UTC")).minusDays(1).toInstant(ZoneOffset.UTC).toEpochMilli();
            case WEEK -> LocalDateTime.now(ZoneId.of("UTC")).minusWeeks(1).toInstant(ZoneOffset.UTC).toEpochMilli();
            case MONTH -> LocalDateTime.now(ZoneId.of("UTC")).minusMonths(1).toInstant(ZoneOffset.UTC).toEpochMilli();
            case SIX_MONTH ->
                    LocalDateTime.now(ZoneId.of("UTC")).minusMonths(6).toInstant(ZoneOffset.UTC).toEpochMilli();
            case YEAR -> LocalDateTime.now(ZoneId.of("UTC")).minusYears(1).toInstant(ZoneOffset.UTC).toEpochMilli();
            case FIVE_YEAR ->
                    LocalDateTime.now(ZoneId.of("UTC")).minusYears(5).toInstant(ZoneOffset.UTC).toEpochMilli();
        };

        log.trace("Расчет startTime из lastTime={}, результат={}", lastTime, startTime);
        return startTime;
    }

    private Long getTimeInMillisFromMarketInterval(final MarketInterval marketInterval) {
        final Long timeInMillis = switch (marketInterval) {
            case ONE_MINUTE -> 60 * 1000L;
            case THREE_MINUTES -> 3 * 60 * 1000L;
            case FIVE_MINUTES -> 5 * 60 * 1000L;
            case FIFTEEN_MINUTES -> 15 * 60 * 1000L;
            case HALF_HOURLY -> 30 * 60 * 1000L;
            case HOURLY -> 60 * 60 * 1000L;
            case TWO_HOURLY -> 2 * 60 * 60 * 1000L;
            case FOUR_HOURLY -> 4 * 60 * 60 * 1000L;
            case SIX_HOURLY -> 6 * 60 * 60 * 1000L;
            case TWELVE_HOURLY -> 12 * 60 * 60 * 1000L;
            case DAILY -> 24 * 60 * 60 * 1000L;
            case WEEKLY -> 7 * 24 * 60 * 60 * 1000L;
            case MONTHLY -> 30 * 24 * 60 * 60 * 1000L;
        };

        log.trace("Расчет timmeInMillis из marketInterval={}, результат={}", marketInterval, timeInMillis);
        return timeInMillis;
    }

    private Object sendRequest(final com.bybit.api.client.domain.market.request.MarketDataRequest request) {
        try {
            return client.getMarketLinesData(request);
        } catch (Exception e) {
            throw new ByBitException("Ошибка отправки запросы к серверу BYBit.", e);
        }
    }
}
