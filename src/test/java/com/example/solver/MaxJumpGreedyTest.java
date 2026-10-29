package com.example.solver;

import com.example.model.MaxJumpAttempt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxJumpGreedyTest {

    @Test
    void counterexampleGoesThroughTwoButOptimumGoesThroughOne() {
        int[] jump = {2, 3, 1, 1, 4};

        MaxJumpAttempt attempt = MaxJumpGreedy.jumpAsFarAsPossible(jump);

        assertTrue(attempt.reachedTarget());
        assertEquals(List.of(0, 2, 3, 4), attempt.path());
        assertEquals(3, attempt.jumps());
        assertEquals(2, FrogJumper.minJumps(jump),
                "оптимум 0 -> 1 -> 4 короче, чем 0 -> 2 -> 3 -> 4");
    }

    @Test
    void canGetStuckEvenWhenAnotherRouteExists() {

        int[] jump = {3, 4, 0, 0, 1};

        MaxJumpAttempt attempt = MaxJumpGreedy.jumpAsFarAsPossible(jump);

        assertFalse(attempt.reachedTarget());
        assertEquals(List.of(0, 3), attempt.path());
        assertTrue(FrogJumper.canReach(jump), "но обход 0 -> 1 -> 4 существует");
        assertEquals(2, FrogJumper.minJumps(jump));
    }

    @Test
    void getsStuckOnZero() {
        MaxJumpAttempt attempt = MaxJumpGreedy.jumpAsFarAsPossible(new int[]{0, 1});

        assertFalse(attempt.reachedTarget());
        assertEquals(List.of(0), attempt.path());
    }

    @Test
    void singlePadIsAlreadyTheGoal() {
        MaxJumpAttempt attempt = MaxJumpGreedy.jumpAsFarAsPossible(new int[]{0});

        assertTrue(attempt.reachedTarget());
        assertEquals(0, attempt.jumps());
        assertEquals(List.of(0), attempt.path());
    }

    @Test
    void longJumpReachesGoalInOneStep() {
        MaxJumpAttempt attempt = MaxJumpGreedy.jumpAsFarAsPossible(new int[]{5, 0, 0, 0, 0});

        assertTrue(attempt.reachedTarget());
        assertEquals(List.of(0, 4), attempt.path());
        assertEquals(1, attempt.jumps());
    }

    @Test
    void invalidInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MaxJumpGreedy.jumpAsFarAsPossible(null));
        assertThrows(IllegalArgumentException.class, () -> MaxJumpGreedy.jumpAsFarAsPossible(new int[0]));
        assertThrows(IllegalArgumentException.class, () -> MaxJumpGreedy.jumpAsFarAsPossible(new int[]{1, -1}));
    }
}
