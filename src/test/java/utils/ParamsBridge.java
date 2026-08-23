package utils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import java.util.HashMap;
import java.util.Map;

/**
 * ParamsBridge
 * - Keeps your original TestNG parameter bridging to System properties (unchanged).
 * - Adds a ThreadLocal K/V store so listeners and utils can read/write per-test context safely.
 * - Provides small helpers for typed access and clearing between tests.
 */
public class ParamsBridge {

    // ---- NEW: per-thread context store (safe for parallel runs) ----
    private static final ThreadLocal<Map<String, String>> TL_CTX =
            ThreadLocal.withInitial(HashMap::new);

    /** Put a key/value into the per-thread context (also mirrors to System props for backward-compat). */
    public static void set(String key, String value) {
        if (key == null || value == null) return;
        TL_CTX.get().put(key, value);
        System.setProperty(key, value); // keep old behavior compatible
    }

    /** Get a value from per-thread context, falling back to System property if absent. */
    public static String get(String key) {
        String v = TL_CTX.get().get(key);
        if (v == null) v = System.getProperty(key);
        return v;
    }

    /** Convenience: get int with default. */
    public static int getInt(String key, int def) {
        try {
            String v = get(key);
            return (v == null || v.isBlank()) ? def : Integer.parseInt(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    /** Clear per-thread context (call at @AfterMethod in base). */
    public static void clear() {
        TL_CTX.get().clear();
    }

    // -------------------- YOUR ORIGINAL BEHAVIOR (UNCHANGED) --------------------
    @Parameters({"udid","serverPort","systemPort","chromedriverPort","noReset"})
    @BeforeClass(alwaysRun = true)
    public void bridge(String udid, String serverPort, String systemPort, String chromePort, String noReset) {
        if (udid != null)          System.setProperty("udid", udid);
        if (serverPort != null)    System.setProperty("serverPort", serverPort);
        if (systemPort != null)    System.setProperty("systemPort", systemPort);
        if (chromePort != null)    System.setProperty("chromedriverPort", chromePort);
        if (noReset != null)       System.setProperty("noReset", noReset);

        // NEW: also mirror to per-thread context for parallel safety
        if (udid != null)          set("udid", udid);
        if (serverPort != null)    set("serverPort", serverPort);
        if (systemPort != null)    set("systemPort", systemPort);
        if (chromePort != null)    set("chromedriverPort", chromePort);
        if (noReset != null)       set("noReset", noReset);
    }
}
