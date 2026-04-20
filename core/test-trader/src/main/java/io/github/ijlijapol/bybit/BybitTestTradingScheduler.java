package io.github.ijlijapol.bybit;

import io.github.ijlijapol.bybit.exception.SchedulerStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Планировщик выполнения тестовых торговых задач на бирже Bybit.
 * <p>
 * Данный класс управляет автоматическим выполнением тестовых торговых операций
 * по расписанию. Задачи выполняются асинхронно в пуле потоков с возможностью
 * ручного включения/выключения автоматического режима.
 * </p>
 * <p>
 * <b>Особенности работы:</b>
 * <ul>
 *   <li>Автоматический запуск задач каждые 15 минут по cron расписанию</li>
 *   <li>Ручное управление планировщиком через методы {@link #enableScheduler()} и {@link #disableScheduler()}</li>
 *   <li>Асинхронное выполнение торговых задач в пуле потоков</li>
 *   <li>При переполнении очереди задач используется политика {@link ThreadPoolExecutor.AbortPolicy}</li>
 *   <li>Для каждой торговой задачи создается новый экземпляр {@link BybitTestTradeExecutor}</li>
 * </ul>
 * </p>
 * <p>
 * <b>Потокобезопасность:</b>
 * Класс является потокобезопасным благодаря использованию {@link AtomicBoolean}
 * для управления состоянием планировщика и потокобезопасному пулу executor'ов.
 * </p>
 *
 * @author ijlijapol
 * @version 1.0
 * @see BybitTestTradeExecutor
 * @see SchedulerStateException
 * @see Scheduled
 */
@Component
@Slf4j
public class BybitTestTradingScheduler {

    private final ExecutorService taskExecutor = new ThreadPoolExecutor(
            1,
            2,
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1),
            new ThreadPoolExecutor.AbortPolicy()
    );
    private final AtomicBoolean schedulerEnabled = new AtomicBoolean(false);
    private final ObjectProvider<BybitTestTradeExecutor> executorProvider;

    public BybitTestTradingScheduler(ObjectProvider<BybitTestTradeExecutor> executorProvider) {
        this.executorProvider = executorProvider;
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void executeTradingJob() {
        if (schedulerEnabled.get()) {
            submitTradingTaskAsync();
        }
    }

    /**
     * Включить автоматическое расписание
     */
    public void enableScheduler() {
        if (!schedulerEnabled.compareAndSet(false, true)) {
            log.warn("Attempt to enable scheduler but it was already enabled");
            throw new SchedulerStateException("Cannot enable scheduler - it is already enabled");
        } else {
            log.info("Scheduler enabled");
        }
    }

    /**
     * Выключить автоматическое расписание
     */
    public void disableScheduler() {
        if (!schedulerEnabled.compareAndSet(true, false)) {
            log.warn("Attempt to disable scheduler but it was already disabled");
            throw new SchedulerStateException("Cannot disable scheduler - it is already disabled");
        } else {
            log.info("Scheduler disabled");
        }
    }

    private void submitTradingTaskAsync() {
        try {
            taskExecutor.execute(() -> {
                try {
                    final BybitTestTradeExecutor executor = executorProvider.getObject();
                    executor.start();
                } catch (Exception e) {
                    log.error("Trading task execution failed", e);
                    throw new RuntimeException("Trading task failed: " + e.getMessage(), e);
                }
            });
        } catch (RejectedExecutionException e) {
            log.warn("Trading task rejected - execution queue is full. " +
                            "Active threads: {}/{}",
                    ((ThreadPoolExecutor) taskExecutor).getActiveCount(),
                    ((ThreadPoolExecutor) taskExecutor).getPoolSize());
        }
    }

    /**
     * Останавливает планировщик и завершает работу пула потоков.
     * <p>
     * Отключает автоматическое расписание, инициирует graceful shutdown
     * пула потоков с ожиданием до 30 секунд. Если задачи не завершаются
     * за это время, выполняется принудительная остановка.
     * </p>
     */
    public void shutdown() {
        schedulerEnabled.set(false);
        taskExecutor.shutdown();
        try {
            if (!taskExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            taskExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
