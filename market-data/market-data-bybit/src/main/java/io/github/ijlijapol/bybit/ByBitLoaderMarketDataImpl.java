package io.github.ijlijapol.bybit;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.response.kline.MarketKlineEntry;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import io.github.ijlijapol.bybit.exception.ByBitException;
import io.github.ijlijapol.bybit.exception.UncorrectedRequestByBit;
import io.github.ijlijapol.bybit.mapper.MapperByBitData;
import io.github.ijlijapol.bybit.mapper.MapperTimeFrame;
import io.github.ijlijapol.bybit.model.request.*;
import io.github.ijlijapol.bybit.model.responce.CandleDTO;
import io.github.ijlijapol.bybit.model.responce.CandlesDTO;
import io.github.ijlijapol.contract.LoaderMarketData;
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
    public CandlesDTO loadRecentMarketData(final RecentMarketDataRequest recentMarketDataRequest) {
        if (recentMarketDataRequest == null) {
            log.error("recentMarketDataRequest is null");
            throw new UncorrectedRequestByBit("Объект recentMarketDataRequest равен null");
        }
        if (recentMarketDataRequest.getSymbol() == null) {
            log.error("recentMarketDataRequest.symbol is null");
            throw new UncorrectedRequestByBit("Symbol в recentMarketDataRequest не может быть null");
        }
        if (recentMarketDataRequest.getLastTime() == null) {
            log.error("recentMarketDataRequest.lastTime is null");
            throw new UncorrectedRequestByBit("LastTime в recentMarketDataRequest не может быть null");
        }
        if (recentMarketDataRequest.getTimeFrame() == null) {
            log.error("recentMarketDataRequest.timeFrame is null");
            throw new UncorrectedRequestByBit("TimeFrame В recentMarketDataRequest не может быть null");
        }

        log.info("Загрузка последних рыночных свеч: symbol-{}, timeFrame={}, lastTime={}",
                recentMarketDataRequest.getSymbol(), recentMarketDataRequest.getTimeFrame(), recentMarketDataRequest.getLastTime());

        final Long startPeriod = getStartTimeFromLastTime(recentMarketDataRequest.getLastTime());
        final Long endDate = LocalDateTime.now(ZoneId.of("UTC")).toInstant(ZoneOffset.UTC).toEpochMilli();
        final MarketInterval marketInterval = MapperTimeFrame.toMarketInterval(recentMarketDataRequest.getTimeFrame());
        final com.bybit.api.client.domain.market.request.MarketDataRequest byBitRequest
                = com.bybit.api.client.domain.market.request.MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol(recentMarketDataRequest.getSymbol().toString())
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
    public CandlesDTO loadMarketDateForPeriodBetween(final MarketDataForPeriodBetweenRequest marketDataForPeriodBetweenRequest) {
        if (marketDataForPeriodBetweenRequest == null) {
            log.error("marketDataForPeriodBetweenRequest is null");
            throw new UncorrectedRequestByBit("Объект marketDataForPeriodBetweenRequest равен null");
        }
        if (marketDataForPeriodBetweenRequest.getSymbol() == null) {
            log.error("marketDataForPeriodBetweenRequest.symbol is null");
            throw new UncorrectedRequestByBit("Symbol в marketDataForPeriodBetweenRequest не может быть null");
        }
        if (marketDataForPeriodBetweenRequest.getTimeFrame() == null) {
            log.error("marketDataForPeriodBetweenRequest.timeFrame is null");
            throw new UncorrectedRequestByBit("TimeFrame in marketDataForPeriodBetweenRequest не может быть null");
        }
        if (marketDataForPeriodBetweenRequest.getStartTime() == null) {
            log.error("marketDataForPeriodBetweenRequest.startTime is null");
            throw new UncorrectedRequestByBit("StartTime in marketDataForPeriodBetweenRequest не может быть null");
        }
        if (marketDataForPeriodBetweenRequest.getEndTime() == null) {
            log.error("marketDataForPeriodBetweenRequest.endTime is null");
            throw new UncorrectedRequestByBit("EndTime in marketDataForPeriodBetweenRequest не может быть null");
        }

        log.info("Загрузка р=рыночных свечей за период: symbol={}, timeFrame={}, start={}, end={}",
                marketDataForPeriodBetweenRequest.getSymbol(),
                marketDataForPeriodBetweenRequest.getTimeFrame(),
                marketDataForPeriodBetweenRequest.getStartTime(),
                marketDataForPeriodBetweenRequest.getEndTime()
        );

        final Long startPeriod = marketDataForPeriodBetweenRequest.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
        final Long endDate = marketDataForPeriodBetweenRequest.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli();
        final MarketInterval marketInterval = MapperTimeFrame.toMarketInterval(marketDataForPeriodBetweenRequest.getTimeFrame());
        final com.bybit.api.client.domain.market.request.MarketDataRequest byBitRequest
                = com.bybit.api.client.domain.market.request.MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol(marketDataForPeriodBetweenRequest.getSymbol().toString())
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
                .startPeriodTime(marketDataForPeriodBetweenRequest.getStartTime())
                .endPeriodTime(marketDataForPeriodBetweenRequest.getEndTime())
                .candles(candleDTOList)
                .build();
    }

    @Override
    public CandleDTO loadLatestCandle(final LastCandleRequest lastDataRequest) {
        if (lastDataRequest == null) {
            log.error("lastDataRequest is null");
            throw new UncorrectedRequestByBit("Объект lastDataRequest равен Null");
        }
        if (lastDataRequest.getTimeFrame() == null) {
            log.error("lastDataRequest.timeFrame is null");
            throw new UncorrectedRequestByBit("TimeFrame in lastDataRequest не может быть null");
        }
        if (lastDataRequest.getSymbol() == null) {
            log.error("lastDataRequest.symbol is null");
            throw new UncorrectedRequestByBit("Symbol in lastDataRequest не может быть null");
        }

        log.info("Загрузка последней свечи: symbol={}, timeFrame={}",
                lastDataRequest.getSymbol(),
                lastDataRequest.getTimeFrame()
        );

        final MarketInterval marketInterval = MapperTimeFrame.toMarketInterval(lastDataRequest.getTimeFrame());
        final Long endDate = LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC).toEpochMilli();
        final Long startTime = endDate - (getTimeInMillisFromMarketInterval(marketInterval));
        final com.bybit.api.client.domain.market.request.MarketDataRequest byBitRequest
                = com.bybit.api.client.domain.market.request.MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol(lastDataRequest.getSymbol().toString())
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

    @Override
    public CandlesDTO loadSelectQuantityCandle(final SelectQuantityCandleRequest selectQuantityCandleRequest) {
        if (selectQuantityCandleRequest.getTimeFrame() == null) {
            log.error("selectQuantityCandleRequest.timeFrame is null");
            throw new UncorrectedRequestByBit("TimeFrame in selectQuantityCandleRequest не может быть null");
        }
        if (selectQuantityCandleRequest.getSymbol() == null) {
            log.error("selectQuantityCandleRequest.symbol is null");
            throw new UncorrectedRequestByBit("Symbol in selectQuantityCandleRequest не может быть null");
        }
        if (selectQuantityCandleRequest.getQuantity() < 2 || selectQuantityCandleRequest.getQuantity() > 1000) {
            log.error("selectQuantityCandleRequest.quantity is less than 2 or more than 1000");
            throw new UncorrectedRequestByBit("Quantity in selectQuantityCandleRequest не может быть меньше 2 или больше 1000");
        }

        log.info("Загрузка последних свечей в количестве {}.", selectQuantityCandleRequest.getQuantity());

        final MarketInterval marketInterval = MapperTimeFrame.toMarketInterval(selectQuantityCandleRequest.getTimeFrame());
        // Вычитаем 1 секунду, чтобы исключить текущую незавершённую свечу (работаем только с подтверждёнными данными)
        final long endDate = LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC).toEpochMilli() - 1000;
        final Long startTime = endDate - (getTimeInMillisFromMarketInterval(marketInterval) * selectQuantityCandleRequest.getQuantity());

        final com.bybit.api.client.domain.market.request.MarketDataRequest byBitRequest
                = com.bybit.api.client.domain.market.request.MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .symbol(selectQuantityCandleRequest.getSymbol().toString())
                .marketInterval(marketInterval)
                .start(startTime)
                .end(endDate)
                .limit(selectQuantityCandleRequest.getQuantity())
                .build();

        log.debug("Запрос последних свечей: limit={}", selectQuantityCandleRequest.getQuantity());
        final List<MarketKlineEntry> marketKlineEntry = MapperByBitData.convertFromResponse(sendRequest(byBitRequest));
        TreeSet<CandleDTO> candleDTOSet = MapperByBitData.convertFromMarketKlineEntry(marketKlineEntry);
        log.info("Получено всего свечей {}.", candleDTOSet.size());
        log.debug("Полученные свечи: {}", candleDTOSet);

        return CandlesDTO.builder()
                .candles(candleDTOSet)
                .build();
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
