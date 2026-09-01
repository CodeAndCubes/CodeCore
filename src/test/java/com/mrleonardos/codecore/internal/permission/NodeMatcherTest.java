package com.mrleonardos.codecore.internal.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class NodeMatcherTest {

    private static final String NODE = "codechat.channel.global.read";
    private static final String VECTORS = "/fixtures/permission-vectors.md";

    @Test
    void exactRuleMatchesOnlyItself() {
        assertTrue(NodeMatcher.matches(NODE, NODE));
        assertFalse(NodeMatcher.matches("codechat.channel.global.write", NODE));
    }

    @Test
    void trailingWildcardCoversEverythingDeeper() {
        assertTrue(NodeMatcher.matches("codechat.*", NODE));
        assertTrue(NodeMatcher.matches("*", NODE));
        assertFalse(NodeMatcher.matches("codeperms.*", NODE));
    }

    @Test
    void middleWildcardCoversExactlyOneSegment() {
        assertTrue(NodeMatcher.matches("codechat.channel.*.read", NODE));
        assertFalse(NodeMatcher.matches("codechat.channel.*.write", NODE));
        assertFalse(NodeMatcher.matches("codechat.*.read", NODE), "звёздочка не должна съедать два сегмента");
    }

    @Test
    void shorterRuleWithoutWildcardDoesNotMatch() {
        assertFalse(NodeMatcher.matches("codechat.channel", NODE));
        assertFalse(NodeMatcher.matches("codechat.channel.global.read.extra", NODE));
    }

    @Test
    void specificityCountsLiteralSegments() {
        assertEquals(0, NodeMatcher.specificity("*"));
        assertEquals(1, NodeMatcher.specificity("codechat.*"));
        assertEquals(3, NodeMatcher.specificity("codechat.channel.*.read"));
        assertEquals(4, NodeMatcher.specificity(NODE));
    }

    @TestFactory
    Stream<DynamicTest> sharedVectorsHold() {
        return DynamicTest.stream(readVectors().iterator(), Vector::describe, vector -> {
            assertEquals(vector.matches, NodeMatcher.matches(vector.pattern, vector.node), vector.describe());
            assertEquals(vector.specificity, NodeMatcher.specificity(vector.pattern), vector.describe());
        });
    }

    @Test
    void sharedVectorFileIsLoadedFully() {
        int loaded = readVectors().size();
        assertTrue(loaded >= 20, "в файле векторов должно быть хотя бы 20 строк, а найдено " + loaded);
    }

    private static List<Vector> readVectors() {
        InputStream stream = NodeMatcherTest.class.getResourceAsStream(VECTORS);
        if (stream == null) {
            throw new IllegalStateException("Vector file is missing: " + VECTORS);
        }
        List<Vector> vectors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String row;
            int number = 0;
            while ((row = reader.readLine()) != null) {
                number++;
                Vector vector = Vector.parse(number, row);
                if (vector != null) {
                    vectors.add(vector);
                }
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Vector file cannot be read: " + VECTORS, failure);
        }
        return vectors;
    }

    static final class Vector {

        final int line;
        final String pattern;
        final String node;
        final boolean matches;
        final int specificity;

        private Vector(int line, String pattern, String node, boolean matches, int specificity) {
            this.line = line;
            this.pattern = pattern;
            this.node = node;
            this.matches = matches;
            this.specificity = specificity;
        }

        static Vector parse(int line, String row) {
            if (row == null || !row.startsWith("|")) {
                return null;
            }
            String[] cells = row.split("\\|", -1);
            if (cells.length < 6) {
                return null;
            }
            String expected = cells[3].trim();
            if (!expected.equals("true") && !expected.equals("false")) {
                return null;
            }
            return new Vector(
                line,
                cells[1].trim(),
                cells[2].trim(),
                Boolean.parseBoolean(expected),
                Integer.parseInt(cells[4].trim()));
        }

        String describe() {
            return "строка " + line + ": " + pattern + " vs " + node + " -> " + matches + ", точность " + specificity;
        }
    }
}
