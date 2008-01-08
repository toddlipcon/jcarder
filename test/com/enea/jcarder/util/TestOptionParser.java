/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2, with a special
 * exception for linking with JUnit. See the accompanying file LICENSE.txt for
 * details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

public class TestOptionParser {
    @Test
    public void testNoArguments() throws InvalidOptionException {
        OptionParser parser = new OptionParser();
        parser.parse(new String[0]);

        assertEquals(new ArrayList(), parser.getArguments());
        assertEquals(new HashMap<String, String>(), parser.getOptions());
    }

    @Test
    public void testOrdinaryArguments() throws InvalidOptionException {
        String[] args = new String[] {"foo", "bar"};
        List<String> arglist = Arrays.asList(args);

        OptionParser parser = new OptionParser();
        parser.parse(args);

        assertEquals(arglist, parser.getArguments());
        assertEquals(new HashMap<String, String>(), parser.getOptions());
    }

    @Test(expected=InvalidOptionException.class)
    public void testInvalidOption() throws InvalidOptionException {
        String[] args = new String[] {"-foo"};

        OptionParser parser = new OptionParser();
        parser.parse(args);
    }

    @Test
    public void testOptionWithoutValue()
    throws InvalidOptionException {
        String[] args = new String[] {"-foo", "bar"};
        ArrayList<String> expectedArguments = new ArrayList<String>();
        expectedArguments.add("bar");
        HashMap<String, String> expectedOptions = new HashMap<String, String>();
        expectedOptions.put("-foo", null);

        OptionParser parser = new OptionParser();
        parser.addOption("-foo", null);
        parser.parse(args);

        assertEquals(expectedArguments, parser.getArguments());
        assertEquals(expectedOptions, parser.getOptions());
    }

    @Test
    public void testOptionWithValue()
    throws InvalidOptionException {
        String[] args = new String[] {"-foo", "fie", "fum"};
        ArrayList<String> expectedArguments = new ArrayList<String>();
        expectedArguments.add("fum");
        HashMap<String, String> expectedOptions = new HashMap<String, String>();
        expectedOptions.put("-foo", "fie");

        OptionParser parser = new OptionParser();
        parser.addOption("-foo x", null);
        parser.parse(args);

        assertEquals(expectedArguments, parser.getArguments());
        assertEquals(expectedOptions, parser.getOptions());
    }

    @Test
    public void testGetHelpText() throws InvalidOptionException {
        OptionParser parser = new OptionParser();
        parser.addOption("-foo X", null);
        parser.addOption("-b", null);
        parser.addOption("-fie Y", "fie using Y");
        parser.addOption("-f", "specify f");
        String expected =
            "  -b\n"
            + "  -f                   specify f\n"
            + "  -fie Y               fie using Y\n"
            + "  -foo X\n";
        assertEquals(expected, parser.getOptionHelp());
    }

    @Test(expected=InvalidOptionException.class)
    public void testOptionMissingValue() throws InvalidOptionException {
        OptionParser parser = new OptionParser();
        parser.addOption("-foo x", null);
        parser.parse(new String[] {"-foo"});
    }
}
