package io.github.ijlijapol;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.response.kline.MarketKlineEntry;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import io.github.ijlijapol.data.market.model.request.*;
import io.github.ijlijapol.data.market.model.responce.CandleDTO;
import io.github.ijlijapol.data.market.model.responce.CandlesDTO;
import io.github.ijlijapol.mapper.MapperByBitData;
import io.github.ijlijapol.mapper.MapperTimeFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ByBitLoaderMarketDataImplTest {

    @Mock
    private BybitApiMarketRestClient mockClient;

    private ByBitLoaderMarketDataImpl loader;

    private final LocalDateTime FIXED_NOW = LocalDateTime.of(2025, 3, 11, 12, 0, 0);
    private final ZoneOffset UTC_OFFSET = ZoneOffset.UTC;

    @BeforeEach
    void setUp() throws Exception {
        loader = new ByBitLoaderMarketDataImpl();
        // Подменяем клиент через рефлексию
        Field clientField = ByBitLoaderMarketDataImpl.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(loader, mockClient);
    }

    @Test
    void loadRecentMarketData_ShouldReturnCandlesDTO_WhenCalledWithValidData() {
        // Given
        RecentMarketData request = RecentMarketData.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .lastTime(LastTime.DAY)
                .build();

        // Мокаем статические методы в правильном порядке
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<MapperTimeFrame> mockedTimeFrame = mockStatic(MapperTimeFrame.class);
             MockedStatic<MapperByBitData> mockedByBitData = mockStatic(MapperByBitData.class)) {

            // Подменяем LocalDateTime.now() чтобы возвращать фиксированное время
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("UTC")))
                    .thenReturn(FIXED_NOW);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneOffset.UTC))
                    .thenReturn(FIXED_NOW);

            long expectedStartTime = FIXED_NOW.minusDays(1).toInstant(UTC_OFFSET).toEpochMilli();
            long expectedEndTime = FIXED_NOW.toInstant(UTC_OFFSET).toEpochMilli();

            mockedTimeFrame.when(() -> MapperTimeFrame.toMarketInterval(TimeFrame.FIFTEEN_MINUTES))
                    .thenReturn(MarketInterval.FIFTEEN_MINUTES);

            List<MarketKlineEntry> mockKlineEntries = createMockKlineEntries(5);
            List<CandleDTO> mockCandleDTOs = createMockCandleDTOs(5);

            when(mockClient.getMarketLinesData(any())).thenReturn("mockResponse");

            mockedByBitData.when(() -> MapperByBitData.convertFromResponse("mockResponse"))
                    .thenReturn(mockKlineEntries);
            mockedByBitData.when(() -> MapperByBitData.convertFromMarketKlineEntry(mockKlineEntries))
                    .thenReturn(mockCandleDTOs);

            // When
            CandlesDTO result = loader.loadRecentMarketData(request);

            // Then
            assertNotNull(result);
            assertEquals(mockCandleDTOs, result.getCandles());
            assertNotNull(result.getStartPeriodTime());
            assertNotNull(result.getEndPeriodTime());

            ArgumentCaptor<com.bybit.api.client.domain.market.request.MarketDataRequest> requestCaptor =
                    ArgumentCaptor.forClass(com.bybit.api.client.domain.market.request.MarketDataRequest.class);
            verify(mockClient).getMarketLinesData(requestCaptor.capture());

            com.bybit.api.client.domain.market.request.MarketDataRequest capturedRequest = requestCaptor.getValue();
            assertEquals(CategoryType.SPOT, capturedRequest.getCategory());
            assertEquals(Symbol.BTCUSDT.toString(), capturedRequest.getSymbol());
            assertEquals(MarketInterval.FIFTEEN_MINUTES, capturedRequest.getMarketInterval());
            assertEquals(expectedStartTime, capturedRequest.getStart());
            assertEquals(expectedEndTime, capturedRequest.getEnd());
            assertEquals(1000, capturedRequest.getLimit());
        }
    }

    @Test
    void loadRecentMarketData_ShouldHandlePagination_WhenMoreThan1000Records() {
        // Given
        RecentMarketData request = RecentMarketData.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .lastTime(LastTime.DAY)
                .build();

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<MapperTimeFrame> mockedTimeFrame = mockStatic(MapperTimeFrame.class);
             MockedStatic<MapperByBitData> mockedByBitData = mockStatic(MapperByBitData.class)) {

            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("UTC")))
                    .thenReturn(FIXED_NOW);
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneOffset.UTC))
                    .thenReturn(FIXED_NOW);

            mockedTimeFrame.when(() -> MapperTimeFrame.toMarketInterval(TimeFrame.FIFTEEN_MINUTES))
                    .thenReturn(MarketInterval.FIFTEEN_MINUTES);

            List<MarketKlineEntry> firstBatchKlines = createMockKlineEntries(1000);
            List<MarketKlineEntry> secondBatchKlines = createMockKlineEntries(500);

            List<CandleDTO> firstBatchCandles = createMockCandleDTOs(1000);
            List<CandleDTO> secondBatchCandles = createMockCandleDTOs(500);

            // Для пагинации нам нужно, чтобы первый ответ содержал 1000 записей
            when(mockClient.getMarketLinesData(any())).thenReturn("mockResponse1", "mockResponse2");

            mockedByBitData.when(() -> MapperByBitData.convertFromResponse("mockResponse1"))
                    .thenReturn(firstBatchKlines);
            mockedByBitData.when(() -> MapperByBitData.convertFromResponse("mockResponse2"))
                    .thenReturn(secondBatchKlines);

            mockedByBitData.when(() -> MapperByBitData.convertFromMarketKlineEntry(firstBatchKlines))
                    .thenReturn(firstBatchCandles);
            mockedByBitData.when(() -> MapperByBitData.convertFromMarketKlineEntry(secondBatchKlines))
                    .thenReturn(secondBatchCandles);

            // When
            CandlesDTO result = loader.loadRecentMarketData(request);

            // Then
            assertNotNull(result);
            assertEquals(1500, result.getCandles().size());
            verify(mockClient, times(2)).getMarketLinesData(any());
        }
    }

    @Test
    void loadMarketDateForPeriodBetween_ShouldReturnCandlesDTO_WhenCalledWithValidData() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 2, 0, 0);

        MarketDataForPeriodBetween request = MarketDataForPeriodBetween.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        long expectedStartMillis = startTime.toInstant(UTC_OFFSET).toEpochMilli();
        long expectedEndMillis = endTime.toInstant(UTC_OFFSET).toEpochMilli();

        try (MockedStatic<MapperTimeFrame> mockedTimeFrame = mockStatic(MapperTimeFrame.class);
             MockedStatic<MapperByBitData> mockedByBitData = mockStatic(MapperByBitData.class)) {

            mockedTimeFrame.when(() -> MapperTimeFrame.toMarketInterval(TimeFrame.FIFTEEN_MINUTES))
                    .thenReturn(MarketInterval.FIFTEEN_MINUTES);

            List<MarketKlineEntry> mockKlineEntries = createMockKlineEntries(5);
            List<CandleDTO> mockCandleDTOs = createMockCandleDTOs(5);

            when(mockClient.getMarketLinesData(any())).thenReturn("mockResponse");

            mockedByBitData.when(() -> MapperByBitData.convertFromResponse("mockResponse"))
                    .thenReturn(mockKlineEntries);
            mockedByBitData.when(() -> MapperByBitData.convertFromMarketKlineEntry(mockKlineEntries))
                    .thenReturn(mockCandleDTOs);

            // When
            CandlesDTO result = loader.loadMarketDateForPeriodBetween(request);

            // Then
            assertNotNull(result);
            assertEquals(mockCandleDTOs, result.getCandles());
            assertEquals(startTime, result.getStartPeriodTime());
            assertEquals(endTime, result.getEndPeriodTime());

            ArgumentCaptor<com.bybit.api.client.domain.market.request.MarketDataRequest> requestCaptor =
                    ArgumentCaptor.forClass(com.bybit.api.client.domain.market.request.MarketDataRequest.class);
            verify(mockClient).getMarketLinesData(requestCaptor.capture());

            com.bybit.api.client.domain.market.request.MarketDataRequest capturedRequest = requestCaptor.getValue();
            assertEquals(expectedStartMillis, capturedRequest.getStart());
            assertEquals(expectedEndMillis, capturedRequest.getEnd());
            assertEquals(1000, capturedRequest.getLimit());
        }
    }

    @Test
    void loadMarketDateForPeriodBetween_ShouldHandlePagination_WhenMoreThan1000Records() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 10, 0, 0);

        MarketDataForPeriodBetween request = MarketDataForPeriodBetween.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        try (MockedStatic<MapperTimeFrame> mockedTimeFrame = mockStatic(MapperTimeFrame.class);
             MockedStatic<MapperByBitData> mockedByBitData = mockStatic(MapperByBitData.class)) {

            mockedTimeFrame.when(() -> MapperTimeFrame.toMarketInterval(TimeFrame.FIFTEEN_MINUTES))
                    .thenReturn(MarketInterval.FIFTEEN_MINUTES);

            List<MarketKlineEntry> firstBatchKlines = createMockKlineEntries(1000);
            List<MarketKlineEntry> secondBatchKlines = createMockKlineEntries(800);

            List<CandleDTO> firstBatchCandles = createMockCandleDTOs(1000);
            List<CandleDTO> secondBatchCandles = createMockCandleDTOs(800);

            when(mockClient.getMarketLinesData(any())).thenReturn("mockResponse1", "mockResponse2");

            mockedByBitData.when(() -> MapperByBitData.convertFromResponse("mockResponse1"))
                    .thenReturn(firstBatchKlines);
            mockedByBitData.when(() -> MapperByBitData.convertFromResponse("mockResponse2"))
                    .thenReturn(secondBatchKlines);

            mockedByBitData.when(() -> MapperByBitData.convertFromMarketKlineEntry(firstBatchKlines))
                    .thenReturn(firstBatchCandles);
            mockedByBitData.when(() -> MapperByBitData.convertFromMarketKlineEntry(secondBatchKlines))
                    .thenReturn(secondBatchCandles);

            // When
            CandlesDTO result = loader.loadMarketDateForPeriodBetween(request);

            // Then
            assertNotNull(result);
            assertEquals(1800, result.getCandles().size());
            verify(mockClient, times(2)).getMarketLinesData(any());
        }
    }

    @Test
    void loadLatestCandle_ShouldReturnCandleDTO_WhenCalledWithValidData() {
        // Given
        MarketDataRequest request = MarketDataRequest.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .build();

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<MapperTimeFrame> mockedTimeFrame = mockStatic(MapperTimeFrame.class);
             MockedStatic<MapperByBitData> mockedByBitData = mockStatic(MapperByBitData.class)) {

            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneOffset.UTC))
                    .thenReturn(FIXED_NOW);

            mockedTimeFrame.when(() -> MapperTimeFrame.toMarketInterval(TimeFrame.FIFTEEN_MINUTES))
                    .thenReturn(MarketInterval.FIFTEEN_MINUTES);

            List<MarketKlineEntry> mockKlineEntries = createMockKlineEntries(1);
            List<CandleDTO> mockCandleDTOs = createMockCandleDTOs(1);

            when(mockClient.getMarketLinesData(any())).thenReturn("mockResponse");

            mockedByBitData.when(() -> MapperByBitData.convertFromResponse("mockResponse"))
                    .thenReturn(mockKlineEntries);
            mockedByBitData.when(() -> MapperByBitData.convertFromMarketKlineEntry(mockKlineEntries))
                    .thenReturn(mockCandleDTOs);

            // When
            CandleDTO result = loader.loadLatestCandle(request);

            // Then
            assertNotNull(result);
            assertEquals(mockCandleDTOs.getFirst(), result);

            ArgumentCaptor<com.bybit.api.client.domain.market.request.MarketDataRequest> requestCaptor =
                    ArgumentCaptor.forClass(com.bybit.api.client.domain.market.request.MarketDataRequest.class);
            verify(mockClient).getMarketLinesData(requestCaptor.capture());

            com.bybit.api.client.domain.market.request.MarketDataRequest capturedRequest = requestCaptor.getValue();
            assertEquals(1, capturedRequest.getLimit());
        }
    }

    @Test
    void getStartTimeFromLastTime_ShouldReturnCorrectTimestamps_ForDifferentLastTimeValues() {
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("UTC")))
                    .thenReturn(FIXED_NOW);

            // Тестируем все значения LastTime в одном тесте, чтобы избежать конфликтов моков
            testLastTimeValue(LastTime.DAY, FIXED_NOW.minusDays(1));
            testLastTimeValue(LastTime.WEEK, FIXED_NOW.minusWeeks(1));
            testLastTimeValue(LastTime.MONTH, FIXED_NOW.minusMonths(1));
            testLastTimeValue(LastTime.SIX_MONTH, FIXED_NOW.minusMonths(6));
            testLastTimeValue(LastTime.YEAR, FIXED_NOW.minusYears(1));
            testLastTimeValue(LastTime.FIVE_YEAR, FIXED_NOW.minusYears(5));
        }
    }

    private void testLastTimeValue(LastTime lastTime, LocalDateTime expectedStartTime) {
        // Сбрасываем mock перед каждым вызовом
        reset(mockClient);

        RecentMarketData request = RecentMarketData.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .lastTime(lastTime)
                .build();

        long expectedMillis = expectedStartTime.toInstant(UTC_OFFSET).toEpochMilli();

        try (MockedStatic<MapperTimeFrame> mockedTimeFrame = mockStatic(MapperTimeFrame.class);
             MockedStatic<MapperByBitData> mockedByBitData = mockStatic(MapperByBitData.class)) {

            mockedTimeFrame.when(() -> MapperTimeFrame.toMarketInterval(TimeFrame.FIFTEEN_MINUTES))
                    .thenReturn(MarketInterval.FIFTEEN_MINUTES);

            when(mockClient.getMarketLinesData(any())).thenReturn("mockResponse");
            mockedByBitData.when(() -> MapperByBitData.convertFromResponse("mockResponse"))
                    .thenReturn(new ArrayList<>());
            mockedByBitData.when(() -> MapperByBitData.convertFromMarketKlineEntry(any()))
                    .thenReturn(new ArrayList<>());

            loader.loadRecentMarketData(request);

            ArgumentCaptor<com.bybit.api.client.domain.market.request.MarketDataRequest> requestCaptor =
                    ArgumentCaptor.forClass(com.bybit.api.client.domain.market.request.MarketDataRequest.class);
            verify(mockClient, times(1)).getMarketLinesData(requestCaptor.capture());

            long actualStart = requestCaptor.getValue().getStart();
            assertEquals(expectedMillis, actualStart, "For LastTime." + lastTime);
        }
    }

    @Test
    void getTimeInMillisFromMarketInterval_ShouldReturnCorrectValues_ForDifferentIntervals() {
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("UTC")))
                    .thenReturn(FIXED_NOW);

            // Тестируем различные интервалы
            testTimeInterval(TimeFrame.FIFTEEN_MINUTES, MarketInterval.FIFTEEN_MINUTES);
            testTimeInterval(TimeFrame.FIFTEEN_MINUTES, MarketInterval.HOURLY);
            testTimeInterval(TimeFrame.FIFTEEN_MINUTES, MarketInterval.DAILY);
        }
    }

    private void testTimeInterval(TimeFrame timeFrame, MarketInterval marketInterval) {
        reset(mockClient);

        RecentMarketData request = RecentMarketData.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(timeFrame)
                .lastTime(LastTime.DAY)
                .build();

        try (MockedStatic<MapperTimeFrame> mockedTimeFrame = mockStatic(MapperTimeFrame.class);
             MockedStatic<MapperByBitData> mockedByBitData = mockStatic(MapperByBitData.class)) {

            mockedTimeFrame.when(() -> MapperTimeFrame.toMarketInterval(timeFrame))
                    .thenReturn(marketInterval);

            when(mockClient.getMarketLinesData(any())).thenReturn("mockResponse");
            mockedByBitData.when(() -> MapperByBitData.convertFromResponse("mockResponse"))
                    .thenReturn(new ArrayList<>());
            mockedByBitData.when(() -> MapperByBitData.convertFromMarketKlineEntry(any()))
                    .thenReturn(new ArrayList<>());

            loader.loadRecentMarketData(request);

            ArgumentCaptor<com.bybit.api.client.domain.market.request.MarketDataRequest> requestCaptor =
                    ArgumentCaptor.forClass(com.bybit.api.client.domain.market.request.MarketDataRequest.class);
            verify(mockClient, times(1)).getMarketLinesData(requestCaptor.capture());

            assertEquals(marketInterval, requestCaptor.getValue().getMarketInterval());
        }
    }

    @Test
    void loadRecentMarketData_ShouldHandleEmptyResponse_WhenNoDataAvailable() {
        // Given
        RecentMarketData request = RecentMarketData.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .lastTime(LastTime.DAY)
                .build();

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<MapperTimeFrame> mockedTimeFrame = mockStatic(MapperTimeFrame.class);
             MockedStatic<MapperByBitData> mockedByBitData = mockStatic(MapperByBitData.class)) {

            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("UTC")))
                    .thenReturn(FIXED_NOW);

            mockedTimeFrame.when(() -> MapperTimeFrame.toMarketInterval(TimeFrame.FIFTEEN_MINUTES))
                    .thenReturn(MarketInterval.FIFTEEN_MINUTES);

            when(mockClient.getMarketLinesData(any())).thenReturn("mockResponse");
            mockedByBitData.when(() -> MapperByBitData.convertFromResponse("mockResponse"))
                    .thenReturn(new ArrayList<>());
            mockedByBitData.when(() -> MapperByBitData.convertFromMarketKlineEntry(new ArrayList<>()))
                    .thenReturn(new ArrayList<>());

            // When
            CandlesDTO result = loader.loadRecentMarketData(request);

            // Then
            assertNotNull(result);
            assertTrue(result.getCandles().isEmpty());
        }
    }

    @Test
    void loadRecentMarketData_ShouldUseCorrectLastTime_ForAllEnumValues() {
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDateTime.when(() -> LocalDateTime.now(ZoneId.of("UTC")))
                    .thenReturn(FIXED_NOW);

            // Проверяем все значения LastTime
            for (LastTime lastTime : LastTime.values()) {
                testLastTimeValue(lastTime, getExpectedTimeForLastTime(lastTime));
            }
        }
    }

    private LocalDateTime getExpectedTimeForLastTime(LastTime lastTime) {
        return switch (lastTime) {
            case DAY -> FIXED_NOW.minusDays(1);
            case WEEK -> FIXED_NOW.minusWeeks(1);
            case MONTH -> FIXED_NOW.minusMonths(1);
            case SIX_MONTH -> FIXED_NOW.minusMonths(6);
            case YEAR -> FIXED_NOW.minusYears(1);
            case FIVE_YEAR -> FIXED_NOW.minusYears(5);
        };
    }

    // Вспомогательные методы для создания тестовых данных
    private List<MarketKlineEntry> createMockKlineEntries(int count) {
        List<MarketKlineEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MarketKlineEntry entry = mock(MarketKlineEntry.class);
            entries.add(entry);
        }
        return entries;
    }

    private List<CandleDTO> createMockCandleDTOs(int count) {
        List<CandleDTO> dtos = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            CandleDTO dto = CandleDTO.builder()
                    .openPrice(BigDecimal.valueOf(50000 + i))
                    .closePrice(BigDecimal.valueOf(50100 + i))
                    .startTime(LocalDateTime.ofEpochSecond(startTime / 1000 + i * 900, 0, UTC_OFFSET))
                    .build();
            dtos.add(dto);
        }
        return dtos;
    }
}