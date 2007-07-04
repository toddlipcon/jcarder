/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2. See the
 * accompanying file LICENSE.txt for details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.util;

import java.io.IOException;
import java.util.Properties;

public final class BuildInformation {

    private BuildInformation() { }

    public static String getShortInfo() {
        try {
            Properties props = loadBuildProperties();
            return "JCarder ("
                   + props.getProperty("build.version")
                   + "/"
                   + props.getProperty("build.number")
                   + ")";
        } catch (IOException e) {
            e.printStackTrace();
            return "JCarder";
        }
    }

    public static void printLongBuildInformation() {
        Properties props;
        try {
            props = loadBuildProperties();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("JCarder -- cards Java programs to keep threads"
                  + " disentangled\n");
        sb.append("\nCopyright (C) 2006-2007 Enea AB\n");
        sb.append("Copyright (C) 2007 Ulrik Svensson\n");
        sb.append("Copyright (C) 2007 Joel Rosdahl\n");
        sb.append("\nVersion: " + props.getProperty("build.version"));
        sb.append("\nBuild  : " + props.getProperty("build.number"));
        sb.append("\nAt     : " + props.getProperty("build.timestamp"));
        sb.append("\nBy     : " + props.getProperty("build.user.name"));
        sb.append("\nOn     : " + props.getProperty("build.os.name"));
        System.out.println(sb.toString());
    }

    private static Properties loadBuildProperties() throws IOException {
        Properties props = new Properties();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        props.load(classLoader.getResourceAsStream("build.properties"));
        return props;
    }
}
