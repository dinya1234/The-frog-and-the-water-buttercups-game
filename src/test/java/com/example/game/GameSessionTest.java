package com.example.game;

import com.example.model.Pond;
import com.example.model.PondAnalysis;
import com.example.solver.FrogAnalyzer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {

    @Test
    void mainScenarioCanBePlayedInTwoOptimalJumps() {
        GameSession session = session(2, 3, 1, 1, 4);

        assertEquals(0, session.currentIndex());
        assertEquals(0, session.jumps());
        assertFalse(session.won());
        assertEquals(2, session.farthestFromCurrent());
        assertEquals(1, session.nextOptimalStep().orElseThrow());
        assertTrue(session.onOptimalPath());

        assertTrue(session.move(1).allowed());
        assertEquals(1, session.currentIndex());
        assertEquals(1, session.jumps());
        assertEquals(4, session.nextOptimalStep().orElseThrow());

        assertTrue(session.move(4).allowed());
        assertTrue(session.won());
        assertEquals(2, session.jumps());
        assertEquals(2, session.analysis().minJumps());
        assertFalse(session.canMove());
        assertTrue(session.nextOptimalStep().isEmpty());
    }

    @Test
    void tooFarAndBackwardMovesAreRejectedWithoutChangingState() {
        GameSession session = session(2, 3, 1, 1, 4);

        GameSession.MoveResult tooFar = session.checkMove(3);
        assertFalse(tooFar.allowed());
        assertTrue(tooFar.message().contains("Слишком далеко"));

        assertFalse(session.move(3).allowed());
        assertEquals(0, session.currentIndex());

        session.move(1);
        assertEquals(1, session.currentIndex());
        assertFalse(session.move(0).allowed());
        assertEquals(1, session.currentIndex());
        assertFalse(session.move(1).allowed());
        assertEquals(1, session.jumps());
    }

    @Test
    void startWithZeroHasNoAvailableMove() {
        GameSession session = session(0, 1);

        assertFalse(session.canMove());
        assertEquals(0, session.farthestFromCurrent());
        assertFalse(session.move(1).allowed());
        assertEquals(0, session.currentIndex());
        assertFalse(session.analysis().reachable());
    }

    @Test
    void playerCanGetStuckOnZeroEvenWhenGoalIsReachable() {
        GameSession session = session(3, 2, 1, 0, 4);

        assertTrue(session.move(3).allowed());
        assertEquals(3, session.currentIndex());
        assertFalse(session.canMove());
        assertFalse(session.move(4).allowed());
        assertEquals(3, session.currentIndex());
    }

    @Test
    void singlePadIsWonImmediately() {
        GameSession session = session(0);

        assertTrue(session.won());
        assertEquals(0, session.jumps());
        assertFalse(session.canMove());
        assertFalse(session.move(0).allowed());
    }

    @Test
    void deviationFromOptimalPathIsReported() {
        GameSession session = session(2, 3, 1, 1, 4);

        assertTrue(session.move(2).allowed());
        assertFalse(session.onOptimalPath());
        assertTrue(session.nextOptimalStep().isEmpty());

        session.reset();
        assertTrue(session.onOptimalPath());
        assertEquals(0, session.jumps());
        assertEquals(1, session.nextOptimalStep().orElseThrow());
    }

    @Test
    void resetAfterWinStartsANewAttempt() {
        GameSession session = session(2, 3, 1, 1, 4);
        session.move(1);
        session.move(4);
        assertTrue(session.won());

        session.reset();

        assertFalse(session.won());
        assertEquals(0, session.currentIndex());
        assertEquals(0, session.jumps());
        assertTrue(session.canMove());
    }

    @Test
    void unreachablePondHasNoOptimalStep() {
        GameSession session = session(3, 2, 1, 0, 4);

        assertFalse(session.analysis().reachable());
        assertTrue(session.nextOptimalStep().isEmpty());
        assertFalse(session.onOptimalPath());
    }

    private static GameSession session(int... jumps) {
        PondAnalysis analysis = FrogAnalyzer.analyze(new Pond("Тест", jumps));
        return new GameSession(analysis);
    }
}
