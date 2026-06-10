package io.github.ilijapol.bybit.mapper;

import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.market.response.kline.MarketKlineEntry;
import com.bybit.api.client.domain.market.response.kline.MarketKlineResult;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.ilijapol.common.model.CandleDTO;
import io.github.ilijapol.common.model.DirectionCandle;
import io.github.ilijapol.common.model.TimeFrame;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class MapperByBitData {

    public static List<MarketKlineEntry> convertFromResponse(final Object response) {
        final ObjectMapper objectMapper = new ObjectMapper();

        final JavaType responseType = TypeFactory.defaultInstance().constructParametricType(
                GenericResponse.class,
                MarketKlineResult.class
        );
        final GenericResponse<MarketKlineResult> genericResponse = objectMapper.convertValue(response, responseType);
        return genericResponse.getResult().getMarketKlineEntries();
    }

    public static TreeSet<CandleDTO> convertFromMarketKlineEntry(
            final List<MarketKlineEntry> marketKlineEntryList,
            final TimeFrame timeFrame
            ) {
        return marketKlineEntryList.stream()
                .map(marketKlineEntry -> {
                    final LocalDateTime startTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(marketKlineEntry.getStartTime()),
                            ZoneId.of("UTC")
                    );
                    final CreateDirectionCandle createDirectionCandle = (String openPriceInput, String closePriceInput) -> {
                        final BigDecimal openPrice = new BigDecimal(openPriceInput);
                        final BigDecimal closePrice = new BigDecimal(closePriceInput);
                        if (openPrice.compareTo(closePrice) > 0) {
                            return DirectionCandle.BEARICH;
                        } else if (openPrice.compareTo(closePrice) < 0) {
                            return DirectionCandle.BULLISH;
                        } else {
                            return DirectionCandle.DOJI;
                        }
                    };
                    return CandleDTO.builder()
                            .timeFrame(timeFrame)
                            .maxPrice(new BigDecimal(marketKlineEntry.getHighPrice()))
                            .minPrice(new BigDecimal(marketKlineEntry.getLowPrice()))
                            .openPrice(new BigDecimal(marketKlineEntry.getOpenPrice()))
                            .closePrice(new BigDecimal(marketKlineEntry.getClosePrice()))
                            .volume(new BigDecimal(marketKlineEntry.getVolume()))
                            .startTime(startTime)
                            .direction(createDirectionCandle.createDirectionCandle(marketKlineEntry.getOpenPrice(), marketKlineEntry.getClosePrice()))
                            .build();
                })
                .collect(Collectors.toCollection(TreeSet::new));
    }
}