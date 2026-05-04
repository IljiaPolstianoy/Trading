package io.github.ijlijapol;

import io.github.ijlijapol.bybit.MarketDataFactory;
import io.github.ijlijapol.bybit.model.Symbol;
import io.github.ijlijapol.bybit.model.order.Side;
import io.github.ijlijapol.bybit.model.order.TradeOrderType;
import io.github.ijlijapol.bybit.model.request.LastCandleRequest;
import io.github.ijlijapol.bybit.model.request.SelectQuantityCandleRequest;
import io.github.ijlijapol.bybit.model.request.TimeFrame;
import io.github.ijlijapol.bybit.model.responce.CandleDTO;
import io.github.ijlijapol.bybit.model.responce.CandlesDTO;
import io.github.ijlijapol.contract.LoaderMarketData;
import io.github.ijlijapol.exception.NotFoundPatternsException;
import io.github.ijlijapol.exception.TestOrderPersistenceException;
import io.github.ijlijapol.model.PatternDto;
import io.github.ijlijapol.model.TestOrder;
import io.github.ijlijapol.repostiory.TestOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
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
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BybitTestTradingExecutor implements TradingExecutor {

    private final LoaderMarketData loaderMarketData;
    private final TestOrderRepository testOrderRepository;
    private final PatternRepository patternTestRepository;
    private final TaskScheduler taskScheduler;

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
     * @param testOrderRepository   репозиторий для сохранения тестовых ордеров
     * @param patternTestRepository репозиторий для получения паттернов торговли
     */
    public BybitTestTradingExecutor(
            final TestOrderRepository testOrderRepository,
            final PatternRepository patternTestRepository
    ) {
        this.loaderMarketData = MarketDataFactory.getByBitStockMarket();
        this.testOrderRepository = testOrderRepository;
        this.patternTestRepository = patternTestRepository;
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        this.taskScheduler = scheduler;
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
     * @throws NotFoundPatternsException     если в базе данных не найдено ни одного паттерна
     * @throws TestOrderPersistenceException если не удалось сохранить тестовый ордер в базу данных
     * @throws RuntimeException              если возникла ошибка при получении рыночных данных или другие непредвиденные ошибки
     */
    public void start() {
        log.info("Start trading task");
        final List<PatternDto> patterns = getPattern();
        final CandlesDTO candlesDTO = getCandles(3);

        if (patterns.isEmpty()) {
            log.error("io.github.ijlijapol.Pattern is empty");
            throw new NotFoundPatternsException("Patterns not found");
        }

        for (PatternDto pattern : patterns) {
            if (isMatchWithPattern(pattern, candlesDTO)) {
                createTestOrderBuy(candlesDTO);
            }
        }
    }

    /**
     * Получает последние рыночные данные с биржи Bybit.
     *
     * <p>
     * Загружает выбранное количество последних свечей для символа BTCUSDT с 15-минутным таймфреймом.
     *
     * @return объект {@link CandlesDTO}, содержащий данные о свечах
     */
    private CandlesDTO getCandles(final int quantity) {
        log.debug("Получения последних свечей в количестве: {}", quantity);
        final SelectQuantityCandleRequest request = SelectQuantityCandleRequest.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .quantity(quantity)
                .build();

        return loaderMarketData.loadSelectQuantityCandle(request);
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
    private void createTestOrderBuy(final CandlesDTO candlesDTO) {
        log.info("Создание тестового ордера.");
        final BigDecimal lastPrice = candlesDTO.getCandles().getLast().getClosePrice();
        final TestOrder testOrderBuy = TestOrder.builder()
                .symbol(Symbol.BTCUSDT)
                .side(Side.BUY)
                .orderType(TradeOrderType.MARKET)
                .price(lastPrice)
                .amount(BigDecimal.ONE)
                .build();

        save(testOrderBuy);

        taskScheduler.schedule(
                this::createTestOrderSell,
                Instant.now().plusSeconds(60)
        );
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
        return patternTestRepository.findAll().stream()
                .map(testPattern ->
                        PatternDto.builder()
                                .candleDirections(testPattern.getCandleDirections())
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
     * @param patternDTO объект паттерна с ожидаемыми направлениями свечей
     * @param candlesDTO объект с данными свечей для проверки
     * @return {@code true} если направления свечей совпадают с паттерном, иначе {@code false}
     */
    private boolean isMatchWithPattern(final PatternDto patternDTO, final CandlesDTO candlesDTO) {
        log.debug("Проверка полученных свечей с всеми паттернами");
        final List<Boolean> candleDirections = candlesDTO.getCandles().stream()
                .map(CandleDTO::isGrowing)
                .toList();

        return patternDTO.getCandleDirections().equals(candleDirections);
    }

    /**
     * Создает тестовый ордер на продажу и сохраняет в базу данных. Вызывается только после ордера на покупку.
     */
    private void createTestOrderSell() {
        log.info("Создание тестового ордера на продажу");

        final LastCandleRequest request = LastCandleRequest.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.ONE_MINUTE)
                .build();
        final CandleDTO candleDTO = loaderMarketData.loadLatestCandle(request);
        final TestOrder testOrderSell = TestOrder.builder()
                .symbol(Symbol.BTCUSDT)
                .side(Side.SELL)
                .orderType(TradeOrderType.MARKET)
                .price(candleDTO.getClosePrice())
                .amount(BigDecimal.ONE)
                .build();

        save(testOrderSell);
    }
}
