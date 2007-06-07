package com.enea.jcarder.util;

import java.io.IOException;
import java.util.Properties;

public final class BuildInformation {

    private BuildInformation() { }

    public static String getShortInfo() {
        try {
            Properties props = loadBuildProperties();
            return "jcarder ("
                   + props.getProperty("build.version")
                   + "/"
                   + props.getProperty("build.number")
                   + ")";
        } catch (IOException e) {
            e.printStackTrace();
            return "jcarder";
        }
    }

    public static void printLongBuildInformation() {
        try {
            System.out.println("\nJCarder by Ulrik Svensson <ulriksv@gmail.com>, Enea");
            Properties props = loadBuildProperties();
            System.out.println("  \nVersion: "
                               + props.getProperty("build.version")
                               + "\nBuild  : " + props.getProperty("build.number")
                               + "\nAt     : "
                               + props.getProperty("build.timestamp")
                               + "\nBy     : "
                               + props.getProperty("build.user.name")
                               + "\nWith   : "
                               + props.getProperty("build.os.name"));
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
