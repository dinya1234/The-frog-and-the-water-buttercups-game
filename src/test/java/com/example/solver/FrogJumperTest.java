package com.example.solver;

import com.example.model.GreedyStep;
import com.example.model.JumpPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrogJumperTest {

    @Test
    void mainScenarioHasKnownAnswers() {
        int[] jump = {2, 3, 1, 1, 4};

        assertTrue(FrogJumper.canReach(jump));
        assertEquals(2, FrogJumper.minJumps(jump));
        assertEquals(2, FrogJumper.minJumpsDp(jump));
    }

    static Stream<Arguments> documentedEdgeCases() {
        return Stream.of(
                Arguments.of(new int[]{0}, true, 0, "одна кувшинка"),
                Arguments.of(new int[]{2, 3, 1, 1, 4}, true, 2, "основной сценарий"),
                Arguments.of(new int[]{3, 2, 1, 0, 4}, false, -1, "обрыв, цель недостижима"),
                Arguments.of(new int[]{1, 1, 1, 1}, true, 3, "все прыжки по одному"),
                Arguments.of(new int[]{0, 1}, false, -1, "старт с нулём"),
                Arguments.of(new int[]{5, 0, 0, 0, 0}, true, 1, "один длинный прыжок"),
                Arguments.of(new int[]{2, 0, 1, 0}, true, 2, "ноль в середине, но есть обход")
        );
    }

    @ParameterizedTest(name = "{3}")
    @MethodSource("documentedEdgeCases")
    void documentedEdgeCasesAgreeInAllImplementations(int[] jump, boolean reachable,
                                                       int expectedJumps, String description) {
        assertEquals(reachable, FrogJumper.canReach(jump), "canReach: " + description);
        assertEquals(expectedJumps, FrogJumper.minJumps(jump), "minJumps: " + description);
        assertEquals(expectedJumps, FrogJumper.minJumpsDp(jump), "minJumpsDp: " + description);
    }

    @Test
    void allZerosAreUnreachableWhenThereAreSeveralPads() {
        assertFalse(FrogJumper.canReach(new int[]{0, 0, 0}));
        assertEquals(-1, FrogJumper.minJumps(new int[]{0, 0, 0}));
        assertEquals(-1, FrogJumper.minJumpsDp(new int[]{0, 0, 0}));
    }

    @Test
    void singlePadNeedsNoJumpsEvenWithZero() {
        assertTrue(FrogJumper.canReach(new int[]{0}));
        assertEquals(0, FrogJumper.minJumps(new int[]{0}));
        assertEquals(0, FrogJumper.minJumpsDp(new int[]{0}));
    }

    @Test
    void hugeJumpDoesNotOverflow() {
        int[] jump = {Integer.MAX_VALUE, 0, 0};

        assertTrue(FrogJumper.canReach(jump));
        assertEquals(1, FrogJumper.minJumps(jump));
        assertEquals(1, FrogJumper.minJumpsDp(jump));
    }

    @Test
    void largeLinearInputDoesNotUseRecursion() {
        int n = 200_000;
        int[] jump = new int[n];
        Arrays.fill(jump, 1);

        assertTrue(FrogJumper.canReach(jump));
        assertEquals(n - 1, FrogJumper.minJumps(jump));

    }

    @Test
    void optimalPathMatchesGreedyAnswer() {
        int[] jump = {2, 3, 1, 1, 4};

        Optional<JumpPath> path = FrogJumper.optimalPath(jump);

        assertTrue(path.isPresent());
        assertEquals(List.of(0, 1, 4), path.get().indices());
        assertValidPath(jump, path.get());
    }

    @Test
    void optimalPathIsEmptyWhenTargetIsUnreachable() {
        assertTrue(FrogJumper.optimalPath(new int[]{3, 2, 1, 0, 4}).isEmpty());
    }

    @Test
    void singlePadPathContainsOnlyStart() {
        Optional<JumpPath> path = FrogJumper.optimalPath(new int[]{0});

        assertTrue(path.isPresent());
        assertEquals(List.of(0), path.get().indices());
        assertEquals(0, path.get().jumps());
    }

    @Test
    void greedyTraceEndsWithGoalOrDeadEnd() {
        List<GreedyStep> reachableTrace = FrogJumper.minJumpsTrace(new int[]{2, 3, 1, 1, 4});
        assertFalse(reachableTrace.isEmpty());
        assertEquals(GreedyStep.Kind.GOAL, reachableTrace.get(reachableTrace.size() - 1).kind());

        List<GreedyStep> unreachableTrace = FrogJumper.minJumpsTrace(new int[]{3, 2, 1, 0, 4});
        assertFalse(unreachableTrace.isEmpty());
        assertEquals(GreedyStep.Kind.DEAD_END, unreachableTrace.get(unreachableTrace.size() - 1).kind());
    }

    @Test
    void randomPondsAgreeWithDpAndPathIsValid() {
        Random random = new Random(12345);

        for (int iteration = 0; iteration < 1000; iteration++) {
            int n = 1 + random.nextInt(25);
            int[] jump = new int[n];
            for (int i = 0; i < n; i++) {
                jump[i] = random.nextInt(8);
            }

            boolean reachable = FrogJumper.canReach(jump);
            int greedy = FrogJumper.minJumps(jump);
            int dp = FrogJumper.minJumpsDp(jump);
            Optional<JumpPath> path = FrogJumper.optimalPath(jump);

            String context = "n=" + n + ", jump=" + Arrays.toString(jump);
            assertEquals(dp, greedy, "жадный и DP разошлись: " + context);
            assertEquals(reachable, greedy >= 0, "canReach и minJumps разошлись: " + context);
            assertEquals(reachable, path.isPresent(), "наличие пути не совпало: " + context);
            if (path.isPresent()) {
                assertEquals(greedy, path.get().jumps(), "длина пути: " + context);
                assertValidPath(jump, path.get());
            }
        }
    }

    @Test
    void invalidArraysAreRejectedExplicitly() {
        assertThrows(IllegalArgumentException.class, () -> FrogJumper.canReach(null));
        assertThrows(IllegalArgumentException.class, () -> FrogJumper.minJumps(new int[0]));
        assertThrows(IllegalArgumentException.class, () -> FrogJumper.minJumpsDp(new int[]{1, -1}));
        assertThrows(IllegalArgumentException.class, () -> FrogJumper.optimalPath(new int[]{-3}));
        assertThrows(IllegalArgumentException.class, () -> FrogJumper.minJumpsTrace(null));
    }

    private static void assertValidPath(int[] jump, JumpPath path) {
        assertEquals(0, path.start(), "путь должен начинаться с 0");
        assertEquals(jump.length - 1, path.end(), "путь должен заканчиваться на последней кувшинке");

        List<Integer> indices = path.indices();
        for (int i = 1; i < indices.size(); i++) {
            int from = indices.get(i - 1);
            int to = indices.get(i);
            assertTrue(to - from <= jump[from],
                    "прыжок " + from + " -> " + to + " длиннее jump[" + from + "]=" + jump[from]);
        }
    }
}
