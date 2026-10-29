package com.example.game;

import com.example.model.JumpPath;
import com.example.model.Pond;
import com.example.model.PondAnalysis;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public final class GameSession {

    private final PondAnalysis analysis;
    private int currentIndex;
    private int jumps;
    private boolean won;

    public GameSession(PondAnalysis analysis) {
        this.analysis = Objects.requireNonNull(analysis, "Разбор пруда не может быть null");
        reset();
    }

    public void reset() {
        currentIndex = 0;
        jumps = 0;
        won = analysis.pond().isSingle();
    }

    public PondAnalysis analysis() {
        return analysis;
    }

    public Pond pond() {
        return analysis.pond();
    }

    public int currentIndex() {
        return currentIndex;
    }

    public int jumps() {
        return jumps;
    }

    public boolean won() {
        return won;
    }

    public int lastIndex() {
        return pond().lastIndex();
    }

    public int farthestFromCurrent() {
        long reach = (long) currentIndex + pond().jump(currentIndex);
        return (int) Math.min(lastIndex(), reach);
    }

    public boolean canMove() {
        return !won && currentIndex < lastIndex() && farthestFromCurrent() > currentIndex;
    }

    public MoveResult checkMove(int target) {
        if (won) {
            return MoveResult.denied("Цель уже достигнута — нажмите «Сброс», чтобы сыграть ещё раз.");
        }
        if (target < 0 || target > lastIndex()) {
            return MoveResult.denied("Такой кувшинки нет: допустимы индексы 0.." + lastIndex() + ".");
        }
        if (target == currentIndex) {
            return MoveResult.denied("Лягушка уже сидит на этой кувшинке.");
        }
        if (target < currentIndex) {
            return MoveResult.denied("Прыгать можно только вперёд.");
        }
        if (target > farthestFromCurrent()) {
            return MoveResult.denied("Слишком далеко: с кувшинки " + currentIndex
                    + " можно прыгнуть максимум до " + farthestFromCurrent() + ".");
        }
        return MoveResult.ok();
    }

    public MoveResult move(int target) {
        MoveResult result = checkMove(target);
        if (!result.allowed()) {
            return result;
        }
        currentIndex = target;
        jumps++;
        won = currentIndex == lastIndex();
        return result;
    }

    public OptionalInt nextOptimalStep() {
        if (won) {
            return OptionalInt.empty();
        }
        Optional<JumpPath> path = analysis.path();
        if (path.isEmpty()) {
            return OptionalInt.empty();
        }
        List<Integer> indices = path.get().indices();
        int position = indices.indexOf(currentIndex);
        if (position < 0 || position + 1 >= indices.size()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(indices.get(position + 1));
    }

    public boolean onOptimalPath() {
        return analysis.path()
                .map(path -> path.indices().contains(currentIndex))
                .orElse(false);
    }

    public record MoveResult(boolean allowed, String message) {

        public static MoveResult ok() {
            return new MoveResult(true, "Прыжок разрешён.");
        }

        public static MoveResult denied(String message) {
            return new MoveResult(false, Objects.requireNonNull(message));
        }
    }
}
