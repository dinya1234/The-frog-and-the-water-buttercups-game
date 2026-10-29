package com.example.model;

public final class JumpArray {

    private JumpArray() {
    }

    public static int[] requireValid(int[] jump) {
        if (jump == null) {
            throw new IllegalArgumentException("Массив прыжков не может быть null");
        }
        if (jump.length == 0) {
            throw new IllegalArgumentException(
                    "Пруд должен содержать хотя бы одну кувшинку (пустой ввод недопустим)");
        }
        for (int i = 0; i < jump.length; i++) {
            if (jump[i] < 0) {
                throw new IllegalArgumentException(
                        "jump[" + i + "] = " + jump[i]
                                + ": прыжок не может быть отрицательным (лягушка прыгает только вперёд)");
            }
        }
        return jump;
    }

    public static int[] copyValidated(int[] jump) {
        return requireValid(jump).clone();
    }
}
