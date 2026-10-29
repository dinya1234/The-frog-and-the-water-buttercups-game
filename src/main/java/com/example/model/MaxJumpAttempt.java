package com.example.model;

import java.util.List;
import java.util.Objects;

public record MaxJumpAttempt(boolean reachedTarget, List<Integer> path) {

    public MaxJumpAttempt {
        Objects.requireNonNull(path, "Путь не может быть null");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Путь не может быть пустым");
        }
        path = List.copyOf(path);
    }

    public int jumps() {
        return path.size() - 1;
    }

    public String format() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                result.append(" -> ");
            }
            result.append(path.get(i));
        }
        return result.toString();
    }
}
