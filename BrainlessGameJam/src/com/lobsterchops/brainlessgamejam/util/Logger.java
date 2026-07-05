package com.lobsterchops.brainlessgamejam.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Logger {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private Logger() {}

    public static void info(String msg) {
        log("INFO ", msg);
    }

    public static void warn(String msg) {
        log("WARN ", msg);
    }

    public static void error(String msg) {
        log("ERROR", msg);
    }

    public static void error(String msg, Throwable t) {
        log("ERROR", msg);
        t.printStackTrace(System.err);
    }

    public static void log(String level, String msg) {
        System.out.printf("[%s] [%s] %s%n", LocalTime.now().format(FMT), level, msg);
    }
}
