package io.github.ijlijapol.bybit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class Trader {

    private final ExecutorService taskExecutor = new ThreadPoolExecutor(
            1,
            2,
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(0),
            new ThreadPoolExecutor.AbortPolicy()
    );
    private final AtomicBoolean schedulerEnabled = new AtomicBoolean(false);

    @Scheduled(cron = "0 */15 * * * *")
    public void task() {
        if (schedulerEnabled.get()) {
            startTask();
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

    private void startTask() {
        try {
            taskExecutor.execute(() -> {
                try {
                    performLongRunningProcess();
                } catch (InterruptedException e) {
                    // Ошибка
                }
            });
        } catch (RejectedExecutionException e) {
            // логика обработки
        }
    }

    private void performLongRunningProcess() throws InterruptedException {
        // Бизнес-логика
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
