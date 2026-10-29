package com.example.model;

import java.util.List;
import java.util.Objects;

public record JumpPath(List<Integer> indices) {

    public JumpPath {
        Objects.requireNonNull(indices, "Путь не может быть null");
        if (indices.isEmpty()) {
            throw new IllegalArgumentException("Путь не может быть пустым");
        }
        indices = List.copyOf(indices);
        for (int i = 1; i < indices.size(); i++) {
            if (indices.get(i) <= indices.get(i - 1)) {
                throw new IllegalArgumentException("Путь должен идти только вперёд: " + indices);
            }
        }
        if (indices.get(0) < 0) {
            throw new IllegalArgumentException("Индекс кувшинки не может быть отрицательным: " + indices);
        }
    }

    public int jumps() {
        return indices.size() - 1;
    }

    public int start() {
        return indices.get(0);
    }

    public int end() {
        return indices.get(indices.size() - 1);
    }

    public String format() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < indices.size(); i++) {
            if (i > 0) {
                result.append(" -> ");
            }
            result.append(indices.get(i));
        }
        return result.toString();
    }
}
