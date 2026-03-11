package io.github.ijlijapol.mapper;

import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.market.response.kline.MarketKlineEntry;
import com.bybit.api.client.domain.market.response.kline.MarketKlineResult;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.ijlijapol.data.market.model.responce.CandleDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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

    public static List<CandleDTO> convertFromMarketKlineEntry(final List<MarketKlineEntry> marketKlineEntryList) {
        return marketKlineEntryList.stream()
                .map(marketKlineEntry -> {
                    final LocalDateTime startTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(marketKlineEntry.getStartTime()),
                            ZoneId.of("UTC")
                    );
                    return CandleDTO.builder()
                            .openPrice(new BigDecimal(marketKlineEntry.getOpenPrice()))
                            .closePrice(new BigDecimal(marketKlineEntry.getClosePrice()))
                            .startTime(startTime)
                            .build();
                })
                .toList();
    }
}