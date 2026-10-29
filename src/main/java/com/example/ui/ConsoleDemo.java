package com.example.ui;

import com.example.io.PondParser;
import com.example.model.Pond;
import com.example.model.PondAnalysis;
import com.example.solver.FrogAnalyzer;

import java.util.ArrayList;
import java.util.List;

public final class ConsoleDemo {

    private static final String SEPARATOR = "-".repeat(74);

    private ConsoleDemo() {
    }

    public static void runAll() {
        printTitle();

        List<PondAnalysis> analyses = new ArrayList<>();
        analyses.add(printScenario(new Pond("Основной пример", 2, 3, 1, 1, 4), true));
        analyses.add(printScenario(new Pond("Обрыв", 3, 2, 1, 0, 4), true));
        analyses.add(printScenario(new Pond("Одна кувшинка", 0), false));
        analyses.add(printScenario(new Pond("Старт с нулём", 0, 1), false));
        analyses.add(printScenario(new Pond("Все прыжки по 1", 1, 1, 1, 1), false));
        analyses.add(printScenario(new Pond("Один длинный прыжок", 5, 0, 0, 0, 0), false));
        analyses.add(printScenario(new Pond("Ноль в середине", 2, 0, 1, 0), false));

        printInvalidInputDemo();

        System.out.println(SEPARATOR);
        System.out.println("Сводка по сценариям (все алгоритмы запускались на одном входе):");
        System.out.print(Renderer.renderSummary(analyses));
        System.out.println();
    }

    public static void runPond(Pond pond) {
        PondAnalysis analysis = FrogAnalyzer.analyze(pond);
        System.out.println(SEPARATOR);
        System.out.println("Разбор пруда: " + pond.name());
        System.out.print(Renderer.render(analysis));
        System.out.print(Renderer.renderTrace(analysis.trace()));
    }

    public static void printInvalidInputDemo() {
        System.out.println(SEPARATOR);
        System.out.println("Явная обработка некорректного ввода:");

        demonstrateError("пустой ввод", "");
        demonstrateError("отрицательный прыжок", "2, -1, 3");
        demonstrateError("нечисловой токен", "2, x, 3");
        demonstrateError("null-строка", null);
        System.out.println();
    }

    private static PondAnalysis printScenario(Pond pond, boolean withTrace) {
        System.out.println(SEPARATOR);
        System.out.println("Сценарий: " + pond.name());
        PondAnalysis analysis = FrogAnalyzer.analyze(pond);
        System.out.print(Renderer.render(analysis));
        if (withTrace) {
            System.out.print(Renderer.renderTrace(analysis.trace()));
        } else {
            System.out.println();
        }
        return analysis;
    }

    private static void demonstrateError(String title, String input) {
        try {
            PondParser.parse(input);
            System.out.println("  " + title + ": ОШИБКА — исключение не было брошено");
        } catch (IllegalArgumentException e) {
            System.out.println("  " + title + ": IllegalArgumentException — " + e.getMessage());
        }
    }

    private static void printTitle() {
        System.out.println("=".repeat(74));
        System.out.println("  ЛЯГУШКА ПО КУВШИНКАМ");
        System.out.println("  Жадный алгоритм + два указателя / скользящее окно + DP-эталон");
        System.out.println("=".repeat(74));
        System.out.println("  На каждой кувшинке написано, насколько далеко с неё можно");
        System.out.println("  прыгнуть. Нужно понять, достижима ли последняя кувшинка,");
        System.out.println("  и найти минимальное число прыжков.");
        System.out.println();
    }
}
