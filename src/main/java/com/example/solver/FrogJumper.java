package com.example.solver;

import com.example.model.GreedyStep;
import com.example.model.JumpArray;
import com.example.model.JumpPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class FrogJumper {

    private static final int INF = Integer.MAX_VALUE;

    private FrogJumper() {
    }
    //жадный
    public static boolean canReach(int[] jump) {
        int n = JumpArray.requireValid(jump).length;
        if (n == 1) {
            return true;
        }

        long farthest = 0;
        for (int i = 0; i < n; i++) {
            if (i > farthest) {
                return false;
            }
            farthest = Math.max(farthest, (long) i + jump[i]);
            if (farthest >= n - 1L) {
                return true;
            }
        }
        return false;
    }

    // два указателя, O(n) / O(1).
    public static int minJumps(int[] jump) {
        return minJumpsInternal(jump, null);
    }

    public static List<GreedyStep> minJumpsTrace(int[] jump) {
        List<GreedyStep> trace = new ArrayList<>();
        minJumpsInternal(jump, trace);
        return List.copyOf(trace);
    }

    private static int minJumpsInternal(int[] jump, List<GreedyStep> trace) {
        int n = JumpArray.requireValid(jump).length;
        if (n == 1) {
            if (trace != null) {
                trace.add(new GreedyStep(0, 0, 0, 0, GreedyStep.Kind.GOAL));
            }
            return 0;
        }

        int jumps = 0;
        long currentEnd = 0;
        long farthest = 0;

        for (int i = 0; i < n - 1; i++) {
            if (i > farthest) {
                if (trace != null) {
                    trace.add(new GreedyStep(i, farthest, currentEnd, jumps, GreedyStep.Kind.DEAD_END));
                }
                return -1;
            }

            farthest = Math.max(farthest, (long) i + jump[i]);

            if (i == currentEnd) {
                jumps++;
                currentEnd = farthest;
                boolean goalReached = currentEnd >= n - 1L;
                if (trace != null) {
                    trace.add(new GreedyStep(i, farthest, currentEnd, jumps,
                            goalReached ? GreedyStep.Kind.GOAL : GreedyStep.Kind.WINDOW_CLOSED));
                }
                if (goalReached) {
                    return jumps;
                }
            } else if (trace != null) {
                trace.add(new GreedyStep(i, farthest, currentEnd, jumps, GreedyStep.Kind.ADVANCE));
            }
        }

        if (trace != null) {
            trace.add(new GreedyStep(n - 1, farthest, currentEnd, jumps, GreedyStep.Kind.DEAD_END));
        }
        return -1;
    }

    // DP-эталон
    public static int minJumpsDp(int[] jump) {
        DpResult result = solveDp(jump);
        return result.dp[result.dp.length - 1] == INF ? -1 : result.dp[result.dp.length - 1];
    }

    public static Optional<JumpPath> optimalPath(int[] jump) {
        DpResult result = solveDp(jump);
        int target = result.dp.length - 1;
        if (result.dp[target] == INF) {
            return Optional.empty();
        }

        LinkedList<Integer> reversed = new LinkedList<>();
        for (int current = target; current != -1; current = result.parent[current]) {
            reversed.addFirst(current);
        }
        return Optional.of(new JumpPath(reversed));
    }

    private static DpResult solveDp(int[] jump) {
        int n = JumpArray.requireValid(jump).length;
        int[] dp = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dp, INF);
        Arrays.fill(parent, -1);
        dp[0] = 0;

        for (int i = 0; i < n; i++) {
            if (dp[i] == INF) {
                continue;
            }
            int reach = (int) Math.min((long) n - 1, (long) i + jump[i]);
            for (int j = i + 1; j <= reach; j++) {
                int candidate = dp[i] + 1;
                if (candidate < dp[j]) {
                    dp[j] = candidate;
                    parent[j] = i;
                }
            }
        }
        return new DpResult(dp, parent);
    }

    private record DpResult(int[] dp, int[] parent) {
    }
}
