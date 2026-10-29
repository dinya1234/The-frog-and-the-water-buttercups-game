package com.example;

import com.example.bench.BenchmarkRunner;
import com.example.io.PondParser;
import com.example.model.Pond;
import com.example.ui.ConsoleDemo;
import com.example.ui.FrogGameFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.Arrays;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.err.println();
            printUsage();
        }
    }

    private static void run(String[] args) {
        if (args.length == 0) {
            if (GraphicsEnvironment.isHeadless()) {
                System.out.println("Графический режим недоступен (headless): запускаю консольную демонстрацию.");
                ConsoleDemo.runAll();
            } else {
                launchGame();
            }
            return;
        }

        String command = args[0];
        switch (command) {
            case "-h", "--help" -> printUsage();
            case "--game", "--gui" -> launchGame();
            case "--console", "--demo" -> ConsoleDemo.runAll();
            case "--bench" -> {
                if (args.length > 1) {
                    BenchmarkRunner.run(Integer.parseInt(args[1]));
                } else {
                    BenchmarkRunner.run();
                }
            }
            case "--pond" -> {
                requireArgument(args, "--pond <список чисел>");
                String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                ConsoleDemo.runPond(PondParser.parse("Свой пруд", text));
            }
            case "--file" -> {
                requireArgument(args, "--file <путь к файлу>");
                ConsoleDemo.runPond(PondParser.readFile(Path.of(args[1])));
            }
            default -> {

                String text = String.join(" ", args);
                Pond pond = PondParser.parse("Свой пруд", text);
                ConsoleDemo.runPond(pond);
            }
        }
    }

    private static void launchGame() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalArgumentException(
                    "графический режим недоступен в headless-среде; используйте --console");
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {

        }
        SwingUtilities.invokeLater(() -> new FrogGameFrame().setVisible(true));
    }

    private static void requireArgument(String[] args, String usage) {
        if (args.length < 2) {
            throw new IllegalArgumentException("не хватает аргумента: " + usage);
        }
    }

    private static void printUsage() {
        System.out.println("""
                Лягушка по кувшинкам — Swing-игра, жадный алгоритм, два указателя и DP.

                Использование:
                  java -jar target/frog-jumper.jar
                      открыть Swing-игру (по умолчанию)

                  java -jar target/frog-jumper.jar --game
                      явно открыть Swing-игру

                  java -jar target/frog-jumper.jar --console
                      полная консольная демонстрация: основной сценарий, контрпример,
                      пограничные случаи и сводная таблица

                  java -jar target/frog-jumper.jar --pond "2,3,1,1,4"
                      разобрать свой пруд в консоли (числа можно разделять запятыми,
                      пробелами или ';')

                  java -jar target/frog-jumper.jar --file data/pond.txt
                      прочитать пруд из файла (комментарии начинаются с '#')

                  java -jar target/frog-jumper.jar --bench [maxN]
                      сравнить время жадного O(n) и DP O(n^2)

                  java -jar target/frog-jumper.jar --help
                      эта справка

                В headless-среде без аргументов автоматически запускается --console.

                Пограничные значения:
                  [0]                 — одна кувшинка, 0 прыжков
                  [0,1]               — старт с нулём, цель недостижима
                  [3,2,1,0,4]         — обрыв, цель недостижима
                  пустой ввод         — отклоняется с понятной ошибкой
                """);
    }
}
