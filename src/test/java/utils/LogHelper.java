package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LogHelper {
    private static final ThreadLocal<Logger> TL = new ThreadLocal<>();
    private static final ThreadLocal<File> TL_FILE = new ThreadLocal<>();

    public static void bindPerTestLogger(String testName) {
        // logback-test.xml already routes to a per-run file; here we keep a pointer
        TL.set(LoggerFactory.getLogger(testName));
        TL_FILE.set(new File(System.getProperty("user.dir") + "/logs/test-run.log"));
    }

    public static void unbindPerTestLogger() {
        TL.remove();
        TL_FILE.remove();
    }

    public static File currentLogFile() { return TL_FILE.get(); }

    public static void info(String msg)  { logger().info(msg); }
    public static void warn(String msg)  { logger().warn(msg); }
    public static void error(String msg) { logger().error(msg); }

    private static Logger logger() {
        Logger l = TL.get();
        return (l != null) ? l : LoggerFactory.getLogger("RUN");
    }
}
