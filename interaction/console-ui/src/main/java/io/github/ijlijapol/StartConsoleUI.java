package io.github.ijlijapol;

import io.github.ijlijapol.bybit.MarketPatternAnalyzer;
import io.github.ijlijapol.model.TradeExecutor;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
public class StartConsoleUI {

    private static Process powerProcess;

    static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(StartConsoleUI.class)
                .web(WebApplicationType.NONE)
                .run(args);
        Scanner sc = new Scanner(System.in);

        startPreventSleep();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nЗавершение приложения...");
            stopPreventSleep();
        }));

        while (true) {
            System.out.println("Доступные команды:");
            System.out.println("1. Поиск паттернов");
            System.out.println("2. Запуск тестовой торговли");
            System.out.println("3. Запуск реальной торговли");
            System.out.println("4. Завершение работы");
            System.out.println("Введите команду:");

            int choice = sc.nextInt();

            final TradingScheduler scheduler = context.getBean(TradingScheduler.class);

            switch (choice) {
                case 1:
                    MarketPatternAnalyzer marketPatternAnalyzer = context.getBean(MarketPatternAnalyzer.class);
                    marketPatternAnalyzer.analyzeMarketdata();
                    break;
                case 2:
                    scheduler.addTradeExecutor(TradeExecutor.BybitTestTradingExecutor);
                    scheduler.enableScheduler();
                    break;
                case 3:
                    scheduler.addTradeExecutor(TradeExecutor.BybitRealTradingExecutor);
                    scheduler.enableScheduler();
                    break;
                case 4:
                    stopPreventSleep();
                    System.exit(0);
            }
        }
    }

    private static void startPreventSleep() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                pb = new ProcessBuilder("powershell.exe",
                        "Start-Process", "powercfg",
                        "-Verb", "RunAs",
                        "-ArgumentList", "'-change -standby-timeout-ac 0'");
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("caffeinate", "-dims");
            } else {
                return; // Linux и другие ОС
            }

            powerProcess = pb.start();
            System.out.println("\nПредотвращение сна включено");

        } catch (IOException e) {
            System.err.println("Не удалось включить предотвращение сна: " + e.getMessage());
        }
    }

    private static void stopPreventSleep() {
        if (powerProcess != null && powerProcess.isAlive()) {
            powerProcess.destroy();
            try {
                if (!powerProcess.waitFor(5, TimeUnit.SECONDS)) {
                    powerProcess.destroyForcibly();
                }
                System.out.println("Предотвращение сна выключено");
            } catch (InterruptedException e) {
                powerProcess.destroyForcibly();
            }
        }
    }
}
