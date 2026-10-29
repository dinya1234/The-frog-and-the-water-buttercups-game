package com.example.ui;

import com.example.model.Pond;
import com.example.model.PondAnalysis;
import com.example.solver.FrogAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RendererTest {

    private static PondAnalysis analyze(String name, int... jumps) {
        return FrogAnalyzer.analyze(new Pond(name, jumps));
    }

    @Test
    void fullReportShowsPondAnswersPathAndCounterexample() {
        String text = Renderer.render(analyze("Основной", 2, 3, 1, 1, 4));

        assertTrue(text.contains("Пруд «Основной»"));
        assertTrue(text.contains("индекс"));
        assertTrue(text.contains("прыжок"));
        assertTrue(text.contains("*"));
        assertTrue(text.contains("canReach"));
        assertTrue(text.contains("minJumps"));
        assertTrue(text.contains("minJumpsDP"));
        assertTrue(text.contains("0 -> 1 -> 4"));
        assertTrue(text.contains("НЕ оптимально"));
    }

    @Test
    void unreachableReportShowsNoPath() {
        String text = Renderer.render(analyze("Обрыв", 3, 2, 1, 0, 4));

        assertTrue(text.contains("нет"));
        assertTrue(text.contains("-1 (недостижимо)"));
        assertFalse(text.contains("оптимальный путь"));
    }

    @Test
    void unreachableMaxJumpReportsThatItGotStuck() {
        String text = Renderer.render(analyze("Старт с нулём", 0, 1));

        assertTrue(text.contains("не дошла: 0 (застряла на нуле)"));
    }

    @Test
    void traceShowsWindowMovement() {
        String trace = Renderer.renderTrace(analyze("Основной", 2, 3, 1, 1, 4).trace());

        assertTrue(trace.contains("currentEnd"));
        assertTrue(trace.contains("farthest"));
        assertTrue(trace.contains("окно сдвинуто"));
        assertTrue(trace.contains("цель накрыта"));
    }

    @Test
    void traceShowsDeadEnd() {
        String trace = Renderer.renderTrace(analyze("Обрыв", 3, 2, 1, 0, 4).trace());

        assertTrue(trace.contains("разрыв"));
        assertTrue(trace.contains("недостижима"));
    }

    @Test
    void summaryMarksNonOptimalMaxJump() {
        List<PondAnalysis> analyses = List.of(
                analyze("Основной", 2, 3, 1, 1, 4),
                analyze("Обрыв", 3, 2, 1, 0, 4),
                analyze("Одна", 0));

        String summary = Renderer.renderSummary(analyses);

        assertTrue(summary.contains("Основной"));
        assertTrue(summary.contains("Обрыв"));
        assertTrue(summary.contains("(!)"));
        assertTrue(summary.contains("застрял"));
    }

    @Test
    void largePondIsTruncatedInsteadOfPrintingThousandsOfColumns() {
        int[] jumps = new int[100];
        Arrays.fill(jumps, 1);

        String text = Renderer.renderPond(Pond.of(jumps), Optional.empty());

        assertTrue(text.contains("..."));
        assertTrue(text.contains("показаны первые и последние"));
    }

    @Test
    void singlePadRendersWithoutPathOrMarkerProblems() {
        String text = Renderer.render(analyze("Одна", 0));

        assertTrue(text.contains("Пруд «Одна»"));
        assertTrue(text.contains("0 прыжков"));
    }
}
