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
        try {
            Properties props = loadBuildProperties();
            StringBuffer sb = new StringBuffer();
            sb.append("\nJCarder by Ulrik Svensson <ulriksv@gmail.com>, Enea");
            sb.append("\nVersion: " + props.getProperty("build.version"));
            sb.append("\nBuild  : " + props.getProperty("build.number"));
            sb.append("\nAt     : " + props.getProperty("build.timestamp"));
            sb.append("\nBy     : " + props.getProperty("build.user.name"));
            sb.append("\nOn     : " + props.getProperty("build.os.name"));
            System.out.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Properties loadBuildProperties() throws IOException {
        Properties props = new Properties();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        props.load(classLoader.getResourceAsStream("build.properties"));
        return props;
    }
}
