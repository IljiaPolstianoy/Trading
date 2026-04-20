package io.github.ijlijapol;

import io.github.ijlijapol.bybit.BybitTestTradingScheduler;
import io.github.ijlijapol.bybit.BybitTradingScheduler;
import io.github.ijlijapol.bybit.MarketPatternAnalyzer;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

@SpringBootApplication
public class StartConsoleUI {
    static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(StartConsoleUI.class)
                .web(WebApplicationType.NONE)
                .run(args);

        while (true) {
            System.out.println("Доступные команды:");
            System.out.println("1. Поиск паттернов");
            System.out.println("2. Запуск тестовой торговли");
            System.out.println("3. Запуск реальной торговли");
            System.out.println("4. Завершение работы");
            System.out.println("Введите команду:");
            Scanner sc = new Scanner(System.in);

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    MarketPatternAnalyzer marketPatternAnalyzer = context.getBean(MarketPatternAnalyzer.class);
                    marketPatternAnalyzer.analyzeMarketdata();
                    break;
                case 2:
                    BybitTestTradingScheduler bybitTestTradingScheduler = context.getBean(BybitTestTradingScheduler.class);
                    bybitTestTradingScheduler.enableScheduler();
                    break;
                case 3:
                    BybitTradingScheduler bybitTradingScheduler = context.getBean(BybitTradingScheduler.class);
                    bybitTradingScheduler.enableScheduler();
                    break;
                case 4:
                    System.exit(0);
            }
        }
    }
}
