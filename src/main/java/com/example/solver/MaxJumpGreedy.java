package com.example.solver;

import com.example.model.JumpArray;
import com.example.model.MaxJumpAttempt;

import java.util.ArrayList;
import java.util.List;

public final class MaxJumpGreedy {

    private MaxJumpGreedy() {
    }

    // Алгоритм-контрпример: жадность по длине прыжка не оптимальна.
    public static MaxJumpAttempt jumpAsFarAsPossible(int[] jump) {
        int n = JumpArray.requireValid(jump).length;
        List<Integer> path = new ArrayList<>();
        path.add(0);

        if (n == 1) {
            return new MaxJumpAttempt(true, path);
        }

        int position = 0;
        while (true) {
            long landing = (long) position + jump[position];
            if (landing <= position) {
                return new MaxJumpAttempt(false, path);
            }

            int next = (int) Math.min(landing, n - 1L);
            path.add(next);
            position = next;

            if (position >= n - 1) {
                return new MaxJumpAttempt(true, path);
            }
        }
    }
}
