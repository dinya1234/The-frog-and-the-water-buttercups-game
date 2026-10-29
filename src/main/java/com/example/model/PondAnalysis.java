package com.example.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record PondAnalysis(
        Pond pond,
        boolean reachable,
        int minJumps,
        int minJumpsDp,
        Optional<JumpPath> path,
        MaxJumpAttempt maxJumpAttempt,
        List<GreedyStep> trace) {

    public PondAnalysis {
        Objects.requireNonNull(pond, "Пруд не может быть null");
        Objects.requireNonNull(path, "Путь не может быть null");
        Objects.requireNonNull(maxJumpAttempt, "Результат max-жадного не может быть null");
        Objects.requireNonNull(trace, "Трассировка не может быть null");
        path = path.isPresent() ? Optional.of(path.get()) : Optional.empty();
        trace = List.copyOf(trace);

        if (reachable != path.isPresent()) {
            throw new IllegalArgumentException(
                    "Несогласованные данные: reachable=" + reachable + ", path=" + path);
        }
        if (reachable && minJumps < 0) {
            throw new IllegalArgumentException("Достижимый пруд не может иметь minJumps < 0");
        }
        if (!reachable && (minJumps != -1 || minJumpsDp != -1)) {
            throw new IllegalArgumentException("Для недостижимого пруда ожидается -1");
        }
        if (path.isPresent() && path.get().jumps() != minJumps) {
            throw new IllegalArgumentException(
                    "Длина пути " + path.get().jumps() + " не совпадает с minJumps " + minJumps);
        }
    }

    public boolean algorithmsAgree() {
        return minJumps == minJumpsDp;
    }

    public boolean isCounterexampleForMaxJump() {
        return maxJumpAttempt.reachedTarget() && maxJumpAttempt.jumps() > minJumps;
    }
}
