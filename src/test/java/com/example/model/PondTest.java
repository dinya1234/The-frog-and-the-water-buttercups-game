package com.example.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PondTest {

    @Test
    void emptyPondIsRejectedExplicitly() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> Pond.of());
        assertTrue(error.getMessage().contains("хотя бы одну кувшинку"));
    }

    @Test
    void nullPondIsRejectedExplicitly() {
        assertThrows(IllegalArgumentException.class, () -> new Pond("Пруд", (int[]) null));
    }

    @Test
    void negativeJumpIsRejectedWithIndex() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Pond("Пруд", 2, -1, 3));

        assertTrue(error.getMessage().contains("jump[1]"));
        assertTrue(error.getMessage().contains("отрицательным"));
    }

    @Test
    void blankNameFallsBackToDefault() {
        assertEquals(Pond.DEFAULT_NAME, new Pond("   ", 1, 2).name());
        assertEquals(Pond.DEFAULT_NAME, new Pond(null, 1, 2).name());
        assertEquals("Мой пруд", new Pond("  Мой пруд  ", 1).name());
    }

    @Test
    void arrayIsDefensivelyCopied() {
        int[] raw = {1, 2, 3};
        Pond pond = new Pond("Пруд", raw);

        raw[0] = 99;
        assertEquals(1, pond.jump(0));

        int[] copy = pond.jumps();
        copy[1] = 99;
        assertEquals(2, pond.jump(1));
        assertArrayEquals(new int[]{1, 2, 3}, pond.jumps());
    }

    @Test
    void sizeAndLastIndexAreConsistent() {
        Pond pond = new Pond("Пруд", 2, 3, 1);

        assertEquals(3, pond.size());
        assertEquals(2, pond.lastIndex());
        assertFalse(pond.isSingle());
        assertTrue(Pond.of(0).isSingle());
    }

    @Test
    void indexOutsidePondIsRejected() {
        Pond pond = Pond.of(1, 2);
        assertThrows(IndexOutOfBoundsException.class, () -> pond.jump(2));
        assertThrows(IndexOutOfBoundsException.class, () -> pond.jump(-1));
    }

    @Test
    void pathMustGoStrictlyForward() {
        assertThrows(IllegalArgumentException.class,
                () -> new JumpPath(List.of(0, 2, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> new JumpPath(List.of()));
    }
}
