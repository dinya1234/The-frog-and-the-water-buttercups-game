package com.example.model;

public record GreedyStep(int index, long farthest, long currentEnd, int jumps, Kind kind) {

    public enum Kind {

        ADVANCE,

        WINDOW_CLOSED,

        GOAL,

        DEAD_END
    }
}
