package com.example.ui;

import com.example.model.GreedyStep;
import com.example.model.JumpPath;
import com.example.model.MaxJumpAttempt;
import com.example.model.Pond;
import com.example.model.PondAnalysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;

public final class Renderer {

    private static final int MAX_POND_COLUMNS = 30;
    private static final String NL = System.lineSeparator();

    private Renderer() {
    }

    public static String render(PondAnalysis analysis) {
        StringBuilder out = new StringBuilder();
        out.append(renderPond(analysis.pond(), analysis.path()));

        resultLine(out, "canReach (жадный, O(n))", analysis.reachable() ? "да" : "нет");
        resultLine(out, "minJumps (два указателя, O(n))",
                analysis.reachable() ? Integer.toString(analysis.minJumps()) : "-1 (недостижимо)");
        resultLine(out, "minJumpsDP (эталон, O(n^2))",
                analysis.reachable() ? Integer.toString(analysis.minJumpsDp()) : "-1 (недостижимо)");
        analysis.path().ifPresent(path ->
                resultLine(out, "оптимальный путь", path.format()));

        MaxJumpAttempt maxJump = analysis.maxJumpAttempt();
        resultLine(out, "«прыгай на максимум»", describeMaxJump(analysis));
        if (analysis.isCounterexampleForMaxJump()) {
            out.append("  ^ это контрпример: max-жадный сделал ")
                    .append(jumpsText(maxJump.jumps()))
                    .append(", а оптимум — ")
                    .append(analysis.minJumps())
                    .append(NL);
        }
        return out.toString();
    }

    public static String renderPond(Pond pond, Optional<JumpPath> path) {
        int n = pond.size();
        int last = n - 1;
        int width = Math.max(3, Integer.toString(last).length() + 1);
        List<Integer> columns = displayIndices(n);
        Set<Integer> pathIndices = new HashSet<>();
        path.ifPresent(p -> pathIndices.addAll(p.indices()));

        StringBuilder out = new StringBuilder();
        out.append("Пруд «").append(pond.name()).append("»: ")
                .append(n).append(' ').append(padsText(n))
                .append(", цель — индекс ").append(last).append(NL).append(NL);

        row(out, "индекс", columns, width, i -> Integer.toString(i));
        row(out, "прыжок", columns, width, i -> Integer.toString(pond.jump(i)));
        if (path.isPresent()) {
            row(out, "путь", columns, width, i -> pathIndices.contains(i) ? "*" : "");
        }

        if (n > MAX_POND_COLUMNS) {
            int edge = (MAX_POND_COLUMNS - 1) / 2;
            out.append("  (показаны первые и последние ").append(edge).append(" кувшинок)").append(NL);
        }
        out.append(NL);
        return out.toString();
    }

    public static String renderTrace(List<GreedyStep> trace) {
        StringBuilder out = new StringBuilder();
        out.append("  Трассировка minJumps (currentEnd / farthest):").append(NL);
        out.append("  шаг |   i | farthest | currentEnd | прыжков | событие").append(NL);
        out.append("  ----+-----+----------+------------+---------+------------------------------------").append(NL);

        int stepNumber = 0;
        for (GreedyStep step : trace) {
            stepNumber++;
            out.append(String.format("  %3d | %3d | %8d | %10d | %7d | %s%n",
                    stepNumber, step.index(), step.farthest(), step.currentEnd(),
                    step.jumps(), describeStep(step)));
        }
        out.append(NL);
        return out.toString();
    }

    public static String renderSummary(List<PondAnalysis> analyses) {
        StringBuilder out = new StringBuilder();
        out.append("  Сценарий                     | canReach | minJumps | DP | max-жадный").append(NL);
        out.append("  -----------------------------+----------+----------+----+------------------------").append(NL);
        for (PondAnalysis analysis : analyses) {
            String maxJump;
            if (!analysis.maxJumpAttempt().reachedTarget()) {
                maxJump = "застрял";
            } else if (analysis.isCounterexampleForMaxJump()) {
                maxJump = analysis.maxJumpAttempt().jumps() + " (!)";
            } else {
                maxJump = Integer.toString(analysis.maxJumpAttempt().jumps());
            }
            out.append(String.format("  %-28s | %-8s | %-8s | %-2s | %s%n",
                    truncate(analysis.pond().name(), 28),
                    analysis.reachable() ? "да" : "нет",
                    analysis.reachable() ? analysis.minJumps() : "-1",
                    analysis.reachable() ? analysis.minJumpsDp() : "-1",
                    maxJump));
        }
        return out.toString();
    }

    private static void row(StringBuilder out, String label, List<Integer> columns,
                            int width, IntFunction<String> value) {
        out.append(String.format("  %-8s|", label));
        for (Integer index : columns) {
            String text = index == null ? "..." : value.apply(index);
            out.append(' ').append(center(text, width));
        }
        out.append(NL);
    }

    private static void resultLine(StringBuilder out, String label, String value) {
        out.append("  ").append(label).append(' ');
        int dots = Math.max(1, 42 - label.length());
        out.append(".".repeat(dots)).append(' ').append(value).append(NL);
    }

    private static String describeMaxJump(PondAnalysis analysis) {
        MaxJumpAttempt attempt = analysis.maxJumpAttempt();
        if (!attempt.reachedTarget()) {
            return "не дошла: " + attempt.format() + " (застряла на нуле)";
        }
        if (attempt.jumps() == 0) {
            return "0 прыжков: " + attempt.format();
        }
        String result = jumpsText(attempt.jumps()) + ": " + attempt.format();
        return analysis.isCounterexampleForMaxJump() ? result + " [НЕ оптимально]" : result;
    }

    private static String describeStep(GreedyStep step) {
        return switch (step.kind()) {
            case ADVANCE -> "расширяем farthest";
            case WINDOW_CLOSED -> "i == currentEnd: новый прыжок, окно сдвинуто";
            case GOAL -> "цель накрыта: currentEnd >= последний индекс — стоп";
            case DEAD_END -> "i > farthest: разрыв, цель недостижима";
        };
    }

    private static List<Integer> displayIndices(int n) {
        List<Integer> columns = new ArrayList<>();
        if (n <= MAX_POND_COLUMNS) {
            for (int i = 0; i < n; i++) {
                columns.add(i);
            }
            return columns;
        }
        int edge = (MAX_POND_COLUMNS - 1) / 2;
        for (int i = 0; i < edge; i++) {
            columns.add(i);
        }
        columns.add(null);
        for (int i = n - edge; i < n; i++) {
            columns.add(i);
        }
        return columns;
    }

    private static String center(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int left = (width - text.length()) / 2;
        int right = width - text.length() - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private static String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }

    private static String padsText(int n) {
        int mod100 = n % 100;
        int mod10 = n % 10;
        if (mod100 >= 11 && mod100 <= 14) {
            return "кувшинок";
        }
        return switch (mod10) {
            case 1 -> "кувшинка";
            case 2, 3, 4 -> "кувшинки";
            default -> "кувшинок";
        };
    }

    private static String jumpsText(int n) {
        int mod100 = n % 100;
        int mod10 = n % 10;
        if (mod100 >= 11 && mod100 <= 14) {
            return n + " прыжков";
        }
        return switch (mod10) {
            case 1 -> n + " прыжок";
            case 2, 3, 4 -> n + " прыжка";
            default -> n + " прыжков";
        };
    }
}
