package com.example.io;

import com.example.model.Pond;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PondParserTest {

    @Test
    void parsesCommaSeparatedList() {
        Pond pond = PondParser.parse("2,3,1,1,4");
        assertArrayEquals(new int[]{2, 3, 1, 1, 4}, pond.jumps());
    }

    @Test
    void parsesSpacesSemicolonsAndBrackets() {
        assertArrayEquals(new int[]{2, 3, 1, 1, 4},
                PondParser.parse("[2 3 1 1 4]").jumps());
        assertArrayEquals(new int[]{2, 3, 1, 1, 4},
                PondParser.parse("2; 3; 1; 1; 4").jumps());
        assertArrayEquals(new int[]{2, 3, 1, 1, 4},
                PondParser.parse("(2, 3, 1, 1, 4)").jumps());
    }

    @Test
    void commentsAndLineBreaksAreIgnored() {
        String text = """
                # контрпример для max-жадного
                2, 3
                1, 1, 4   # хвост строки — комментарий
                """;

        Pond pond = PondParser.parse("Мой пруд", text);

        assertEquals("Мой пруд", pond.name());
        assertArrayEquals(new int[]{2, 3, 1, 1, 4}, pond.jumps());
    }

    @Test
    void emptyAndBlankInputAreRejected() {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> PondParser.parse(""))
                .getMessage().contains("Пустой ввод"));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> PondParser.parse("   \n\t"))
                .getMessage().contains("Пустой ввод"));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> PondParser.parse("# только комментарий"))
                .getMessage().contains("Пустой ввод"));
    }

    @Test
    void emptyBracketsAreRejectedAsEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> PondParser.parse("[]"));
        assertThrows(IllegalArgumentException.class, () -> PondParser.parse("[   ]"));
    }

    @Test
    void nullInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PondParser.parse((String) null));
    }

    @Test
    void utf8BomIsIgnored() {
        Pond pond = PondParser.parse("\uFEFF2, 3, 1");
        assertArrayEquals(new int[]{2, 3, 1}, pond.jumps());
    }

    @Test
    void nonNumericTokenReportsPosition() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> PondParser.parse("2, x, 3"));

        assertTrue(error.getMessage().contains("позиции 2"));
        assertTrue(error.getMessage().contains("'x'"));
    }

    @Test
    void negativeJumpIsRejected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> PondParser.parse("2, -1, 3"));

        assertTrue(error.getMessage().contains("отрицательным"));
    }

    @Test
    void readsPondFromFileAndUsesFileStemAsName(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("counterexample.txt");
        Files.writeString(file, "# пример\n2, 3, 1, 1, 4\n");

        Pond pond = PondParser.readFile(file);

        assertEquals("counterexample", pond.name());
        assertArrayEquals(new int[]{2, 3, 1, 1, 4}, pond.jumps());
    }

    @Test
    void missingFileIsReportedAsUserFriendlyError(@TempDir Path directory) {
        Path missing = directory.resolve("no-such-file.txt");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> PondParser.readFile(missing));

        assertTrue(error.getMessage().contains("Не удалось прочитать файл"));
    }
}
