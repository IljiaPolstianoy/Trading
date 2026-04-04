package io.github.ijlijapol.bybit;


import io.github.ijlijapol.MarketDataFactory;
import io.github.ijlijapol.bybit.exception.NotFoundPatternsException;
import io.github.ijlijapol.bybit.exception.TestOrderPersistenceException;
import io.github.ijlijapol.bybit.model.PatternDto;
import io.github.ijlijapol.bybit.model.TestOrder;
import io.github.ijlijapol.bybit.repository.PatternRepository;
import io.github.ijlijapol.bybit.repository.TestOrderRepository;
import io.github.ijlijapol.contract.LoaderMarketData;
import io.github.ijlijapol.model.Symbol;
import io.github.ijlijapol.model.order.Side;
import io.github.ijlijapol.model.order.TradeOrderType;
import io.github.ijlijapol.model.request.LastTime;
import io.github.ijlijapol.model.request.RecentMarketDataRequest;
import io.github.ijlijapol.model.request.TimeFrame;
import io.github.ijlijapol.model.responce.CandleDTO;
import io.github.ijlijapol.model.responce.CandlesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Исполнитель тестовых торговых операций на бирже Bybit.
 * <p>
 * Данный класс отвечает за выполнение тестовой торговой логики: получение рыночных данных,
 * сравнение их с заданными паттернами и создание тестовых ордеров при совпадении паттернов.
 * </p>
 * <p>
 * <b>Важно:</b> Класс имеет прототипный скоуп ({@link ConfigurableBeanFactory#SCOPE_PROTOTYPE}),
 * что означает создание нового экземпляра при каждом запросе из Spring контейнера.
 * Это обеспечивает потокобезопасность при параллельном выполнении торговых задач.
 * </p>
 * <p>
 * <b>Особенности работы:</b>
 * <ul>
 *   <li>Загружает последние рыночные данные для символа BTCUSDT с 15-минутным таймфреймом</li>
 *   <li>Получает все паттерны из базы данных</li>
 *   <li>Сравнивает направление свечей (рост/падение) с паттернами</li>
 *   <li>При совпадении создает тестовый MARKET ордер на покупку объемом 1 BTC</li>
 * </ul>
 * </p>
 *
 * @author ijlijapol
 * @version 1.0
 * @see TestOrderRepository
 * @see PatternRepository
 * @see LoaderMarketData
 * @see NotFoundPatternsException
 * @see TestOrderPersistenceException
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BybitTestTradeExecutor {

    private final LoaderMarketData loaderMarketData;
    private final TestOrderRepository testOrderRepository;
    private final PatternRepository patternRepository;

    /**
     * Конструктор исполнителя тестовых торгов.
     * <p>
     * Инициализирует загрузчик рыночных данных через фабрику {@link MarketDataFactory}
     * и внедряет необходимые репозитории для работы с базой данных.
     * </p>
     * <p>
     * При создании каждого экземпляра в лог записывается его хэш-код для отслеживания
     * создания новых объектов и отладки.
     * </p>
     *
     * @param testOrderRepository репозиторий для сохранения тестовых ордеров
     * @param patternRepository   репозиторий для получения паттернов торговли
     */
    public BybitTestTradeExecutor(
            final TestOrderRepository testOrderRepository,
            final PatternRepository patternRepository
    ) {
        this.loaderMarketData = MarketDataFactory.getByBitStockMarket();
        this.testOrderRepository = testOrderRepository;
        this.patternRepository = patternRepository;
        log.info("Created new BybitTestTradeExecutor instance: {}", this.hashCode());
    }

    /**
     * Запускает процесс выполнения тестовой торговой задачи.
     * <p>
     * Метод выполняет следующие шаги:
     * <ol>
     *   <li>Получает все паттерны из базы данных</li>
     *   <li>Загружает актуальные рыночные данные (последние свечи)</li>
     *   <li>Проверяет наличие паттернов в базе данных</li>
     *   <li>Для каждого паттерна проверяет соответствие с текущими рыночными данными</li>
     *   <li>При совпадении создает тестовый ордер на покупку</li>
     * </ol>
     * </p>
     *
     * @throws NotFoundPatternsException если в базе данных не найдено ни одного паттерна
     * @throws TestOrderPersistenceException если не удалось сохранить тестовый ордер в базу данных
     * @throws RuntimeException если возникла ошибка при получении рыночных данных или другие непредвиденные ошибки
     */
    public void start() {
        log.info("Start trading task");
        final List<PatternDto> patternDtos = getPattern();
        final CandlesDTO candlesDTO = getCandles();

        if (patternDtos.isEmpty()) {
            log.error("Pattern is empty");
            throw new NotFoundPatternsException("Patterns not found");
        }

        for (PatternDto patternDto : patternDtos) {
            if (isMatchWithPattern(patternDto, candlesDTO)) {
                createTestOrder(candlesDTO);
            }
        }
    }

    /**
     * Получает последние рыночные данные с биржи Bybit.
     * <p>
     * Загружает данные для символа BTCUSDT с 15-минутным таймфреймом за последний день.
     * </p>
     * <p>
     * <b>TODO:</b> При добавлении нового метода в интерфейс {@link LoaderMarketData}
     * необходимо изменить реализацию данного метода.
     * </p>
     *
     * @return объект {@link CandlesDTO}, содержащий данные о свечах
     */
    // TODO: при добавление нового метода в LoaderMarketData изменить запрос
    private CandlesDTO getCandles() {
        log.debug("Получения последних трех свечей");
        final RecentMarketDataRequest request = RecentMarketDataRequest.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .lastTime(LastTime.DAY)
                .build();

        return loaderMarketData.loadRecentMarketData(request);
    }

    /**
     * Создает и сохраняет тестовый ордер на покупку.
     * <p>
     * Создает MARKET ордер на покупку BTCUSDT по последней цене закрытия свечи
     * объемом 1 BTC. Цена ордера берется из последней свечи полученных рыночных данных.
     * </p>
     *
     * @param candlesDTO объект с данными свечей, из которого берется последняя цена закрытия
     * @throws TestOrderPersistenceException если не удалось сохранить ордер в базу данных
     */
    private void createTestOrder(final CandlesDTO candlesDTO) {
        log.info("Создания тестового ордера.");
        final BigDecimal lastPrice = candlesDTO.getCandles().getLast().getClosePrice();
        final TestOrder testOrder = TestOrder.builder()
                .symbol(Symbol.BTCUSDT)
                .side(Side.BUY)
                .orderType(TradeOrderType.MARKET)
                .price(lastPrice)
                .amount(BigDecimal.ONE)
                .build();

        save(testOrder);
    }

    /**
     * Сохраняет тестовый ордер в базу данных.
     * <p>
     * Оборачивает вызов репозитория в try-catch блок для преобразования
     * возможных исключений в кастомное исключение {@link TestOrderPersistenceException}.
     * </p>
     *
     * @param testOrder объект тестового ордера для сохранения
     * @throws TestOrderPersistenceException если произошла ошибка при сохранении в базу данных
     */
    private void save(final TestOrder testOrder) {
        log.debug("Сохранения тестового ордера в базу данных");
        try {
            testOrderRepository.save(testOrder);
        } catch (Exception ex) {
            log.error("Ошибка сохранения тестового ордера: {} в базу данных.", testOrder);
            throw new TestOrderPersistenceException("Ошибка сохранения тестового ордера: " +
                    testOrder + "  базу данных", ex);
        }
    }

    /**
     * Получает все паттерны из базы данных и преобразует их в DTO объекты.
     * <p>
     * Загружает все паттерны из репозитория и преобразует каждый паттерн
     * в объект {@link PatternDto}, содержащий только необходимые поля (направления свечей).
     * </p>
     *
     * @return список DTO объектов паттернов, может быть пустым, но не null
     * @throws RuntimeException если возникла ошибка при обращении к базе данных
     */
    private List<PatternDto> getPattern() {
        log.debug("Получения всех паттернов из БД");
        return patternRepository.findAll().stream()
                .map(pattern ->
                        PatternDto.builder()
                                .candleDirections(pattern.getCandleDirections())
                                .build()
                )
                .toList();
    }

    /**
     * Проверяет соответствие рыночных данных заданному паттерну.
     * <p>
     * Сравнивает последовательность направлений свечей (рост/падение) из рыночных данных
     * с последовательностью, заданной в паттерне. Паттерн считается совпавшим, если
     * последовательности полностью идентичны.
     * </p>
     *
     * @param patternDto объект паттерна с ожидаемыми направлениями свечей
     * @param candlesDTO объект с данными свечей для проверки
     * @return {@code true} если направления свечей совпадают с паттерном, иначе {@code false}
     */
    private boolean isMatchWithPattern(final PatternDto patternDto, final CandlesDTO candlesDTO) {
        log.debug("Проверка полученных свечей с всеми паттернами");
        final List<Boolean> candleDirections = candlesDTO.getCandles().stream()
                .map(CandleDTO::isGrowing)
                .toList();

        return patternDto.getCandleDirections().equals(candleDirections);
    }
}
