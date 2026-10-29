package com.example.bench;

import com.example.solver.FrogJumper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BenchmarkRunner {

    private static final int DEFAULT_MAX_N = 16_000;
    private static final int HARD_MAX_N = 32_000;
    private static final int HUGE_N = 1_000_000;

    private BenchmarkRunner() {
    }

    public static void run() {
        run(DEFAULT_MAX_N);
    }

    public static void run(int requestedMaxN) {
        if (requestedMaxN < 1) {
            throw new IllegalArgumentException("Размер массива должен быть положительным: " + requestedMaxN);
        }
        int maxN = Math.min(requestedMaxN, HARD_MAX_N);

        System.out.println("=== Замер: жадный minJumps (O(n)) против DP (O(n^2)) ===");
        System.out.println("Массивы со случайными прыжками 0..n/2 (много рёбер для DP).");
        System.out.println();
        System.out.println("         n |  жадный, мс |     DP, мс | ответ (жадный/DP)");
        System.out.println("-----------+-------------+------------+-------------------");

        int[] warmup = randomJumps(1_000, 1L);
        FrogJumper.minJumps(warmup);
        FrogJumper.minJumpsDp(warmup);

        for (int n : sizes(maxN)) {
            int[] jumps = randomJumps(n, 42L + n);
            long greedyStart = System.nanoTime();
            int greedy = FrogJumper.minJumps(jumps);
            long greedyNanos = System.nanoTime() - greedyStart;

            long dpStart = System.nanoTime();
            int dp = FrogJumper.minJumpsDp(jumps);
            long dpNanos = System.nanoTime() - dpStart;

            if (greedy != dp) {
                throw new IllegalStateException(
                        "Жадный и DP разошлись при n=" + n + ": " + greedy + " vs " + dp);
            }
            System.out.printf("%10d | %11.3f | %10.3f | %d / %d%n",
                    n, millis(greedyNanos), millis(dpNanos), greedy, dp);
        }

        int[] huge = randomJumps(HUGE_N, 2026L);
        long hugeStart = System.nanoTime();
        int hugeAnswer = FrogJumper.minJumps(huge);
        long hugeNanos = System.nanoTime() - hugeStart;
        System.out.printf("%10d | %11.3f | %10s | %d / -%n",
                HUGE_N, millis(hugeNanos), "не считался", hugeAnswer);

        System.out.println();
        System.out.println("DP при n = 1 000 000 потребовал бы порядка 10^12 релаксаций:");
        System.out.println("именно он «ломается» первым при росте входа, а жадный остаётся O(n).");
        System.out.println("Память: жадный — O(1), DP — O(n) на dp и parent.");
        System.out.println();
    }

    private static List<Integer> sizes(int maxN) {
        List<Integer> sizes = new ArrayList<>();
        for (int n = 1_000; n <= maxN; n *= 2) {
            sizes.add(n);
        }
        if (sizes.isEmpty() || sizes.get(sizes.size() - 1) != maxN) {
            sizes.add(maxN);
        }
        return sizes;
    }

    private static int[] randomJumps(int n, long seed) {
        Random random = new Random(seed);
        int[] jumps = new int[n];
        int maxJump = Math.max(2, n / 2);
        for (int i = 0; i < n; i++) {
            jumps[i] = random.nextInt(maxJump + 1);
        }
        if (n > 1 && jumps[0] == 0) {
            jumps[0] = 1;
        }
        return jumps;
    }

    private static double millis(long nanos) {
        return nanos / 1_000_000.0;
    }
}
