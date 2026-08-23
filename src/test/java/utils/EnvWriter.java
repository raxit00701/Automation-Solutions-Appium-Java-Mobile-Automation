package utils;

import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Writes allure-results/environment.properties so Allure shows env info.
 * - Thread-safe
 * - Idempotent (write only when content changed)
 * - Supports "write once per run" if you choose
 */
public class EnvWriter {

    private static final AtomicBoolean WRITTEN_ONCE = new AtomicBoolean(false);

    // Use the SAME resolver as AllureHistory
    private static String resultsDir() {
        return System.getProperty(
                "allure.results.directory",
                System.getProperty("user.dir") + "/allure-results"
        );
    }

    /** Write if missing or content changed. Safe for parallel runs. */
    public static boolean writeIfChanged(AndroidDriver driver, String author) throws Exception {
        String content = buildContent(driver, author);
        File env = new File(resultsDir() + File.separator + "environment.properties");

        synchronized (EnvWriter.class) {
            env.getParentFile().mkdirs();
            if (env.exists()) {
                String existing = FileUtils.readFileToString(env, StandardCharsets.UTF_8);
                if (existing.equals(content)) return false; // up-to-date → skip
            }
            FileUtils.writeStringToFile(env, content, StandardCharsets.UTF_8);
            return true;
        }
    }

    /**
     * Strict "write once per run" wrapper.
     * Call this instead of writeIfChanged(...) if you NEVER want later tests/devices
     * to rewrite environment.properties.
     */
    public static boolean writeOnce(AndroidDriver driver, String author) throws Exception {
        if (WRITTEN_ONCE.compareAndSet(false, true)) {
            return writeIfChanged(driver, author);
        }
        return false;
    }

    // -------------------- helpers --------------------

    private static String buildContent(AndroidDriver driver, String author) {
        StringBuilder sb = new StringBuilder();
        prop(sb, "Author", author);

        if (driver != null) {
            Object udid            = safeCap(driver, "udid");
            Object device          = safeCap(driver, "deviceName");
            Object platformName    = safeCap(driver, "platformName");
            Object platformVersion = safeCap(driver, "platformVersion");
            Object appPkg          = safeCap(driver, "appPackage");

            prop(sb, "Device",            String.valueOf(device));
            prop(sb, "UDID",              String.valueOf(udid));
            prop(sb, "Platform",          String.valueOf(platformName));
            prop(sb, "Platform Version",  String.valueOf(platformVersion));
            prop(sb, "App Package",       String.valueOf(appPkg));
        }
        return sb.toString();
    }

    private static Object safeCap(AndroidDriver driver, String key) {
        try { return driver.getCapabilities().getCapability(key); }
        catch (Exception ignored) { return "NA"; }
    }

    private static void prop(StringBuilder sb, String k, String v) {
        sb.append(k).append('=').append(Objects.toString(v, "NA")).append('\n');
        // Allure expects simple key=value lines; avoid spaces around '='
    }
}
