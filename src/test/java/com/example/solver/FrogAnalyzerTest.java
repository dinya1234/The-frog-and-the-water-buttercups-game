package com.example.solver;

import com.example.model.JumpPath;
import com.example.model.Pond;
import com.example.model.PondAnalysis;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrogAnalyzerTest {

    @Test
    void counterexampleReportContainsBothAnswersAndPath() {
        PondAnalysis analysis = FrogAnalyzer.analyze(new Pond("Контрпример", 2, 3, 1, 1, 4));

        assertTrue(analysis.reachable());
        assertTrue(analysis.algorithmsAgree());
        assertEquals(2, analysis.minJumps());
        assertEquals(2, analysis.minJumpsDp());
        assertEquals(List.of(0, 1, 4), analysis.path().orElseThrow().indices());
        assertEquals(3, analysis.maxJumpAttempt().jumps());
        assertTrue(analysis.isCounterexampleForMaxJump());
    }

    @Test
    void unreachablePondHasNoPath() {
        PondAnalysis analysis = FrogAnalyzer.analyze(new Pond("Обрыв", 3, 2, 1, 0, 4));

        assertFalse(analysis.reachable());
        assertEquals(-1, analysis.minJumps());
        assertEquals(-1, analysis.minJumpsDp());
        assertTrue(analysis.path().isEmpty());
        assertFalse(analysis.maxJumpAttempt().reachedTarget());
        assertFalse(analysis.isCounterexampleForMaxJump());
    }

    @Test
    void singlePadIsHandledWithoutSpecialCasesInCaller() {
        PondAnalysis analysis = FrogAnalyzer.analyze(new Pond("Одна", 0));

        assertTrue(analysis.reachable());
        assertEquals(0, analysis.minJumps());
        assertEquals(0, analysis.minJumpsDp());
        assertEquals(new JumpPath(List.of(0)), analysis.path().orElseThrow());
        assertEquals(0, analysis.maxJumpAttempt().jumps());
    }

    @Test
    void everyScenarioIsInternallyConsistent() {
        List<Pond> ponds = List.of(
                new Pond("Основной", 2, 3, 1, 1, 4),
                new Pond("Обрыв", 3, 2, 1, 0, 4),
                new Pond("Одна", 0),
                new Pond("Ноль", 0, 1),
                new Pond("Единицы", 1, 1, 1, 1),
                new Pond("Длинный", 5, 0, 0, 0, 0),
                new Pond("Середина", 2, 0, 1, 0)
        );

        for (Pond pond : ponds) {
            PondAnalysis analysis = FrogAnalyzer.analyze(pond);
            assertTrue(analysis.algorithmsAgree(), "алгоритмы разошлись: " + pond);
            assertEquals(analysis.reachable(), analysis.path().isPresent(), "путь: " + pond);
            analysis.path().ifPresent(path ->
                    assertEquals(analysis.minJumps(), path.jumps(), "длина пути: " + pond));
        }
    }
}
