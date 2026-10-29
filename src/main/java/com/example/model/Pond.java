package com.example.model;

import java.util.Arrays;
import java.util.Objects;

public final class Pond {

    public static final String DEFAULT_NAME = "Пруд";

    private final String name;
    private final int[] jumps;

    public Pond(String name, int... jumps) {
        this.jumps = JumpArray.copyValidated(jumps);
        this.name = name == null || name.isBlank() ? DEFAULT_NAME : name.trim();
    }

    public static Pond of(int... jumps) {
        return new Pond(DEFAULT_NAME, jumps);
    }

    public String name() {
        return name;
    }

    public int size() {
        return jumps.length;
    }

    public int lastIndex() {
        return jumps.length - 1;
    }

    public boolean isSingle() {
        return jumps.length == 1;
    }

    public int jump(int index) {
        Objects.checkIndex(index, jumps.length);
        return jumps[index];
    }

    public int[] jumps() {
        return jumps.clone();
    }

    @Override
    public String toString() {
        return name + " " + Arrays.toString(jumps);
    }
}
