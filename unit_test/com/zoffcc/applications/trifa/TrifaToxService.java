package com.zoffcc.applications.trifa;

public class TrifaToxService {

    final static boolean CTOXCORE_JNI_LOGGING = false;

    // Called by C code for logging
    public static void logger(int level, String message) {
        if (CTOXCORE_JNI_LOGGING) {
            if (message != null && message.endsWith("\n")) {
                message = message.substring(0, message.length() - 1);
            }
            System.out.println("\u001B[34m[TOX LOG " + level + "]\u001B[0m " + message);
        }
    }

    // Called by C code to safely convert byte arrays to Java Strings.
    // HINT: decode explicitly as UTF-8, exactly like the real implementation in
    // src/main/kotlin/com/zoffcc/applications/trifa/TrifaToxService.kt (safe_string).
    // Using the platform default charset here would make this stub diverge from
    // production and fail on non-UTF-8 locales (e.g. LANG=C).
    public static String safe_string(byte[] data) {
        if (data == null) return null;
        return new String(data, java.nio.charset.StandardCharsets.UTF_8);
    }
}
