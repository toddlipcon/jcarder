package com.enea.jcarder.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TestOptionFormatter {
    @Test
    public void testFormatUnwrappedLine() {
        OptionFormatter of = new OptionFormatter(2, 6, 15);
        StringBuilder sb = new StringBuilder();

        String expected = "  -x  foo bar\n";
        of.format(sb, "-x", "foo bar");
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testFormatWrappedLine() {
        OptionFormatter of = new OptionFormatter(2, 6, 15);
        StringBuilder sb = new StringBuilder();

        String expected = "  -x  foo bar\n      gazonk\n";
        of.format(sb, "-x", "foo bar gazonk");
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testAddSpace() {
        StringBuilder sb = new StringBuilder();
        OptionFormatter.addSpace(sb, 0);
        assertEquals("", sb.toString());
        OptionFormatter.addSpace(sb, 2);
        assertEquals("  ", sb.toString());
    }

    @Test
    public void testWrapText() {
        String text = "foo gazonk x y";
        Map<Integer, String> expected = new HashMap<Integer, String>();
        expected.put(1, "[foo, gazonk, x, y]");
        expected.put(2, "[foo, gazonk, x, y]");
        expected.put(3, "[foo, gazonk, x y]");
        expected.put(4, "[foo, gazonk, x y]");
        expected.put(5, "[foo, gazonk, x y]");
        expected.put(6, "[foo, gazonk, x y]");
        expected.put(7, "[foo, gazonk, x y]");
        expected.put(8, "[foo, gazonk x, y]");
        expected.put(9, "[foo, gazonk x, y]");
        expected.put(10, "[foo gazonk, x y]");
        expected.put(11, "[foo gazonk, x y]");
        expected.put(12, "[foo gazonk x, y]");
        expected.put(13, "[foo gazonk x, y]");
        expected.put(14, "[foo gazonk x y]");
        expected.put(15, "[foo gazonk x y]");
        for (int length : expected.keySet()) {
            assertEquals(expected.get(length),
                         OptionFormatter.wrapText(text, length).toString());
        }
    }

    @Test
    public void testIndentText() {
        StringBuilder sb = new StringBuilder();
        List<String> lines = new ArrayList<String>();
        lines.add("foo");
        lines.add(" bar");
        lines.add("gazonk");
        String expected = " foo\n     bar\n    gazonk\n";
        OptionFormatter.indentText(sb, lines, 4, 1);
        assertEquals(sb.toString(), expected);
    }
}
