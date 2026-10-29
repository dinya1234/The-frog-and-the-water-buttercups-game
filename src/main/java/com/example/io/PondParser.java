package com.example.io;

import com.example.model.Pond;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class PondParser {

    private PondParser() {
    }

    public static Pond parse(String text) {
        return parse(Pond.DEFAULT_NAME, text);
    }

    public static Pond parse(String name, String text) {
        String data = stripComments(text);
        if (data.isBlank()) {
            throw new IllegalArgumentException(
                    "Пустой ввод: ожидался список неотрицательных чисел, например [2, 3, 1, 1, 4]");
        }

        String cleaned = data
                .replace('[', ' ')
                .replace(']', ' ')
                .replace('(', ' ')
                .replace(')', ' ')
                .trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(
                    "Пустой ввод: ожидался список неотрицательных чисел, например [2, 3, 1, 1, 4]");
        }
        String[] tokens = cleaned.split("[,\\s;]+");

        int[] jumps = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            try {
                jumps[i] = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Не число или число вне диапазона int в позиции " + (i + 1)
                                + ": '" + token + "'", e);
            }
            if (jumps[i] < 0) {
                throw new IllegalArgumentException(
                        "jump[" + i + "] = " + jumps[i] + ": прыжок не может быть отрицательным");
            }
        }
        return new Pond(name, jumps);
    }

    public static Pond readFile(Path path) {
        Objects.requireNonNull(path, "Путь к файлу не может быть null");
        try {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            return parse(fileStem(path), text);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Не удалось прочитать файл '" + path + "': " + e.getMessage(), e);
        }
    }

    private static String stripComments(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Пустой ввод: текст не может быть null");
        }
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
            text = text.substring(1);
        }
        StringBuilder data = new StringBuilder();
        for (String line : text.split("\\R")) {
            int comment = line.indexOf('#');
            if (comment >= 0) {
                line = line.substring(0, comment);
            }
            if (!line.isBlank()) {
                data.append(line).append(' ');
            }
        }
        return data.toString();
    }

    private static String fileStem(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return Pond.DEFAULT_NAME;
        }
        String name = fileName.toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
