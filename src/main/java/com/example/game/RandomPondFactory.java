package com.example.game;

import com.example.model.Pond;
import com.example.solver.FrogJumper;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public final class RandomPondFactory {

    public static final int MIN_PADS = 9;
    public static final int MAX_PADS = 13;
    private static final int ATTEMPTS = 500;

    private RandomPondFactory() {
    }

    public static Pond random(Random random) {
        Objects.requireNonNull(random, "Генератор случайных чисел не может быть null");

        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int size = MIN_PADS + random.nextInt(MAX_PADS - MIN_PADS + 1);
            int[] jumps = new int[size];
            for (int i = 0; i < size - 1; i++) {
                jumps[i] = random.nextInt(4);
            }
            jumps[0] = Math.max(1, jumps[0]);
            jumps[size - 1] = 0;

            if (FrogJumper.canReach(jumps) && FrogJumper.minJumps(jumps) >= 3) {
                return new Pond("Случайный пруд", jumps);
            }
        }

        int[] fallback = new int[MAX_PADS];
        Arrays.fill(fallback, 1);
        fallback[0] = 2;
        fallback[MAX_PADS - 1] = 0;
        return new Pond("Случайный пруд", fallback);
    }
}
