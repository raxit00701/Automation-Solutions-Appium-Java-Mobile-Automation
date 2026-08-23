// MediaManager.java
package utils;

import org.testng.ITestResult;

import java.io.File;

public class MediaManager {
    private static final ThreadLocal<String> TL_TEST_DIR = new ThreadLocal<>();
    private static final ThreadLocal<String> TL_DEVICE_KEY = new ThreadLocal<>();
    private static final boolean PER_TEST = Boolean.getBoolean("media.perTest"); // default false


    public static void setDeviceKey(String deviceKey) {
        TL_DEVICE_KEY.set(sanitize(deviceKey == null ? "unknown-device" : deviceKey));
    }

    public static String getDeviceKey() {
        String k = TL_DEVICE_KEY.get();
        return k == null ? "unknown-device" : k;
    }

    public static void createPerTestFolders(String testName) {
        String base = System.getProperty("user.dir");
        String deviceKey = getDeviceKey();

        // device-scoped roots
        File shots = new File(base + File.separator + "media" + File.separator + deviceKey + File.separator + "screenshots");
        File vids  = new File(base + File.separator + "media" + File.separator + deviceKey + File.separator + "videos");
        shots.mkdirs();
        vids.mkdirs();

        if (PER_TEST) {
            String testDir = base + File.separator + "media" + File.separator + deviceKey + File.separator + sanitize(testName);
            new File(testDir).mkdirs();
            TL_TEST_DIR.set(testDir);
        } else {
            TL_TEST_DIR.remove(); // don’t track a per-test folder
        }
    }

    public static File currentScreenshotPath(String tag, ITestResult result) {
        String prefix = namePrefix(result);
        String name = prefix + "_shot_" + tag + ".png";
        return new File(screenshotsRoot(), name);
    }

    // Overload kept for older calls
    public static File currentScreenshotPath(String tag) {
        String name = "shot_" + System.currentTimeMillis() + "_" + tag + ".png";
        return new File(screenshotsRoot(), name);
    }

    public static File currentVideoPath(ITestResult result) {
        String prefix = namePrefix(result);
        String name = prefix + "_video.mp4";
        return new File(videosRoot(), name);
    }

    // Overload kept for older calls
    public static File currentVideoPath() {
        String name = "video_" + System.currentTimeMillis() + ".mp4";
        return new File(videosRoot(), name);
    }

    private static File screenshotsRoot() {
        String base = System.getProperty("user.dir");
        String deviceKey = getDeviceKey();
        return new File(base + File.separator + "media" + File.separator + deviceKey + File.separator + "screenshots");
    }

    private static File videosRoot() {
        String base = System.getProperty("user.dir");
        String deviceKey = getDeviceKey();
        return new File(base + File.separator + "media" + File.separator + deviceKey + File.separator + "videos");
    }

    private static String sanitize(String s) { return s.replaceAll("[^a-zA-Z0-9._-]", "_"); }

    private static String namePrefix(ITestResult r) {
        String cls = r.getMethod().getTestClass().getName();
        String mtd = r.getMethod().getMethodName();
        long tid = Thread.currentThread().getId();
        long ts = System.currentTimeMillis();
        return sanitize(cls + "#" + mtd + "_T" + tid + "_" + ts);
    }
}
