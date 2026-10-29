package com.example.solver;

import com.example.model.GreedyStep;
import com.example.model.JumpPath;
import com.example.model.MaxJumpAttempt;
import com.example.model.Pond;
import com.example.model.PondAnalysis;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class FrogAnalyzer {

    private FrogAnalyzer() {
    }

    public static PondAnalysis analyze(Pond pond) {
        Objects.requireNonNull(pond, "Пруд не может быть null");
        int[] jump = pond.jumps();

        boolean reachable = FrogJumper.canReach(jump);
        int minJumps = FrogJumper.minJumps(jump);
        int minJumpsDp = FrogJumper.minJumpsDp(jump);

        if (minJumps != minJumpsDp) {
            throw new IllegalStateException(
                    "Жадный и DP разошлись: minJumps=" + minJumps + ", minJumpsDp=" + minJumpsDp
                            + " для " + pond);
        }
        if (reachable != (minJumps >= 0)) {
            throw new IllegalStateException(
                    "canReach=" + reachable + " противоречит minJumps=" + minJumps + " для " + pond);
        }

        Optional<JumpPath> path = FrogJumper.optimalPath(jump);
        if (path.isPresent() != reachable) {
            throw new IllegalStateException(
                    "Наличие пути не совпало с canReach для " + pond);
        }
        if (path.isPresent() && path.get().jumps() != minJumps) {
            throw new IllegalStateException(
                    "Путь длиной " + path.get().jumps() + " не совпал с minJumps=" + minJumps);
        }

        MaxJumpAttempt maxJumpAttempt = MaxJumpGreedy.jumpAsFarAsPossible(jump);
        if (!reachable && maxJumpAttempt.reachedTarget()) {
            throw new IllegalStateException(
                    "Max-жадный дошёл до цели в недостижимом пруду " + pond);
        }

        List<GreedyStep> trace = FrogJumper.minJumpsTrace(jump);
        return new PondAnalysis(pond, reachable, minJumps, minJumpsDp, path, maxJumpAttempt, trace);
    }
}
