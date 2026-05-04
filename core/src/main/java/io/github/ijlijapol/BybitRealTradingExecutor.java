package io.github.ijlijapol;


import io.github.ijlijapol.bybit.ExchangeConnectorFactory;
import io.github.ijlijapol.bybit.MarketDataFactory;
import io.github.ijlijapol.bybit.model.Symbol;
import io.github.ijlijapol.bybit.model.order.OrderDTO;
import io.github.ijlijapol.bybit.model.order.Side;
import io.github.ijlijapol.bybit.model.order.TradeOrderType;
import io.github.ijlijapol.bybit.model.request.LastCandleRequest;
import io.github.ijlijapol.bybit.model.request.SelectQuantityCandleRequest;
import io.github.ijlijapol.bybit.model.request.TimeFrame;
import io.github.ijlijapol.bybit.model.responce.CandleDTO;
import io.github.ijlijapol.bybit.model.responce.CandlesDTO;
import io.github.ijlijapol.contract.ApiClientConnector;
import io.github.ijlijapol.contract.ExchangeConnector;
import io.github.ijlijapol.contract.LoaderMarketData;
import io.github.ijlijapol.exception.NotFoundPatternsException;
import io.github.ijlijapol.exception.OrderPersistenceException;
import io.github.ijlijapol.exception.WalletBalanceException;
import io.github.ijlijapol.model.Constant;
import io.github.ijlijapol.model.Order;
import io.github.ijlijapol.model.PatternDto;
import io.github.ijlijapol.repostiory.OrderRepository;
import io.github.ijlijapol.repostiory.PatternRepository;
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
 * Исполнитель торговых операций на бирже Bybit.
 * <p>
 * Данный класс отвечает за выполнение торговой логики: получение рыночных данных,
 * сравнение их с заданными паттернами и создание ордеров при совпадении паттернов.
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
 *   <li>При совпадении создает тестовый MARKET ордер на покупку объемом BTC</li>
 * </ul>
 * </p>
 *
 * @author ijlijapol
 * @version 1.0
 * @see OrderRepository
 * @see PatternRepository
 * @see LoaderMarketData
 * @see NotFoundPatternsException
 * @see OrderPersistenceException
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BybitRealTradingExecutor implements TradingExecutor {

    private final LoaderMarketData loaderMarketData;
    private final OrderRepository orderRepository;
    private final PatternRepository patternRepository;
    private final ExchangeConnector tradeClient;
    private final ApiClientConnector walletClient;
    private final TaskScheduler taskScheduler;

    /**
     * Конструктор исполнителя торгов.
     * <p>
     * Инициализирует загрузчик рыночных данных через фабрику {@link MarketDataFactory}
     * и внедряет необходимые репозитории для работы с базой данных.
     * </p>
     * <p>
     * При создании каждого экземпляра в лог записывается его хэш-код для отслеживания
     * создания новых объектов и отладки.
     * </p>
     *
     * @param orderRepository          репозиторий для сохранения тестовых ордеров
     * @param patternRepository        репозиторий для получения паттернов торговли
     * @param exchangeConnectorFactory фабрика клиентов биржи Bybit
     */
    public BybitRealTradingExecutor(
            final OrderRepository orderRepository,
            final PatternRepository patternRepository,
            final ExchangeConnectorFactory exchangeConnectorFactory
    ) {
        this.loaderMarketData = MarketDataFactory.getByBitStockMarket();
        this.orderRepository = orderRepository;
        this.patternRepository = patternRepository;
        this.tradeClient = exchangeConnectorFactory.getExchangeConnectorByBit(Constant.API_KEY, Constant.API_SECRET);
        this.walletClient = exchangeConnectorFactory.getApiClientConnectorByBit(Constant.API_KEY, Constant.API_SECRET);
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        this.taskScheduler = scheduler;
        log.info("Created new BybitTestTradeExecutor instance: {}", this.hashCode());
    }

    /**
     * Запускает процесс выполнения торговой задачи.
     * <p>
     * Метод выполняет следующие шаги:
     * <ol>
     *   <li>Получает все паттерны из базы данных</li>
     *   <li>Загружает актуальные рыночные данные (последние свечи)</li>
     *   <li>Проверяет наличие паттернов в базе данных</li>
     *   <li>Для каждого паттерна проверяет соответствие с текущими рыночными данными</li>
     *   <li>При совпадении создает ордер на покупку</li>
     * </ol>
     * </p>
     *
     * @throws NotFoundPatternsException если в базе данных не найдено ни одного паттерна
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
                createOrder(candlesDTO);
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
    private CandlesDTO getCandles() {
        log.debug("Получения последних трех свечей");
        final SelectQuantityCandleRequest request = SelectQuantityCandleRequest.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.FIFTEEN_MINUTES)
                .quantity(3)
                .build();

        return loaderMarketData.loadSelectQuantityCandle(request);
    }

    /**
     * Создает и сохраняет ордер на покупку.
     * <p>
     * Создает MARKET ордер на покупку BTCUSDT по последней цене закрытия свечи
     * объемом равным количеству моенты длоя оплаты на кошельке.
     * Цена ордера берется из последней свечи полученных рыночных данных.
     * </p>
     *
     * @param candlesDTO объект с данными свечей, из которого берется последняя цена закрытия
     * @throws OrderPersistenceException если не удалось сохранить ордер в базу данных
     */
    private void createOrder(final CandlesDTO candlesDTO) {
        log.info("Создания ордера.");
        final BigDecimal lastPrice = candlesDTO.getCandles().getLast().getClosePrice();
        final OrderDTO orderDTO = OrderDTO.builder()
                .symbol(Symbol.BTCUSDT)
                .side(Side.BUY)
                .orderType(TradeOrderType.MARKET)
                .price(lastPrice)
                .amount(getBalance())
                .build();

        log.debug("Отправка orderDTO: {}, на биржу", orderDTO);
        tradeClient.createNewOrder(orderDTO);
        save(Order.builder()
                .symbol(orderDTO.getSymbol())
                .side(orderDTO.getSide())
                .orderType(orderDTO.getOrderType())
                .price(orderDTO.getPrice())
                .amount(orderDTO.getAmount())
                .build());

        taskScheduler.schedule(
                this::createOrderSell,
                Instant.now().plusSeconds(60 * 15) // Ждем 15 минут
        );
    }

    /**
     * Сохраняет ордер в базу данных.
     * <p>
     * Оборачивает вызов репозитория в try-catch блок для преобразования
     * возможных исключений в кастомное исключение {@link OrderPersistenceException}.
     * </p>
     *
     * @param order объект ордера для сохранения
     * @throws OrderPersistenceException если произошла ошибка при сохранении в базу данных
     */
    private void save(final Order order) {
        log.debug("Сохранения ордера в базу данных");
        try {
            orderRepository.save(order);
        } catch (Exception ex) {
            log.error("Ошибка сохранения ордера: {} в базу данных.", order);
            throw new OrderPersistenceException("Ошибка сохранения ордера: " +
                    order + "  базу данных", ex);
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

    /**
     * Получает баланс в выбранной монете
     *
     * @return {@code BigDecimal}  баланс
     */
    private BigDecimal getBalance() {
        log.debug("Получение баланса по моенете USDT");
        final BigDecimal balance = walletClient.getWalletActiveBalance("USDT");

        if (balance == null) {
            throw new WalletBalanceException("Ошибка получения баланса");
        }
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            throw new WalletBalanceException("Баланс равен 0");
        }

        return balance;
    }

    /**
     * Создает ордер на продажу, отправляет на биржу и сохраняет в базу данных. Вызывается только после ордера на покупку.
     */
    private void createOrderSell() {
        log.info("Создание ордера на продажу");

        final LastCandleRequest request = LastCandleRequest.builder()
                .symbol(Symbol.BTCUSDT)
                .timeFrame(TimeFrame.ONE_MINUTE)
                .build();
        final CandleDTO candleDTO = loaderMarketData.loadLatestCandle(request);
        final OrderDTO orderDTOSell = OrderDTO.builder()
                .symbol(Symbol.BTCUSDT)
                .side(Side.SELL)
                .orderType(TradeOrderType.MARKET)
                .price(candleDTO.getClosePrice())
                .amount(BigDecimal.ONE)
                .build();

        tradeClient.createNewOrder(orderDTOSell);

        save(Order.builder()
                .symbol(orderDTOSell.getSymbol())
                .side(orderDTOSell.getSide())
                .orderType(orderDTOSell.getOrderType())
                .price(orderDTOSell.getPrice())
                .amount(orderDTOSell.getAmount())
                .build());
    }
}
