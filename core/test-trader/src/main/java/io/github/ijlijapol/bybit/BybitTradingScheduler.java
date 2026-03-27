package io.github.ijlijapol.bybit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class BybitTradingScheduler {

    private final ExecutorService taskExecutor = new ThreadPoolExecutor(
            1,
            2,
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(0),
            new ThreadPoolExecutor.AbortPolicy()
    );
    private final AtomicBoolean schedulerEnabled = new AtomicBoolean(false);

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
            // Ошибка
        };
    }

    /**
     * Выключить автоматическое расписание
     */
    public void disableScheduler() {
        if (schedulerEnabled.compareAndSet(true, false)) {
            // Ошибка
        };
    }

    private void submitTradingTaskAsync() {
        try {
            taskExecutor.execute(() -> {
                try {
                    // бизнес логика торговли
                    final BybitTestTradeExecutor executor = new BybitTestTradeExecutor();
                    executor.start();
                } catch (InterruptedException e) {
                    // Ошибка
                }
            });
        } catch (RejectedExecutionException e) {
            // логика обработки
        }
    }

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
