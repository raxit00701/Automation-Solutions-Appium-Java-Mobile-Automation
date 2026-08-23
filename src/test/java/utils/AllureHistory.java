package utils;

import org.apache.commons.io.FileUtils;
import java.io.File;

/**
 * Copies previous run's report history into the current results BEFORE tests start,
 * so the next report shows trends.
 *
 * System props (override as needed):
 *  - allure.results.directory  (default: ./allure-results)
 *  - allure.report.directory   (default: ./allure-report)
 */
public class AllureHistory {

    private static String resultsDir() {
        return System.getProperty(
                "allure.results.directory",
                System.getProperty("user.dir") + "/allure-results"
        );
    }

    private static String reportDir() {
        return System.getProperty(
                "allure.report.directory",
                System.getProperty("user.dir") + "/allure-report"
        );
    }

    /** To show Trends/History: copy previous run's
     *    <reportDir>/history  ->  <resultsDir>/history
     * Call this in ISuite.onStart BEFORE tests produce new results.
     */
    public static void injectHistoryIfAvailable() throws Exception {
        File reportHistory = new File(reportDir() + "/history");
        File resultsHistory = new File(resultsDir() + "/history");

        if (reportHistory.exists() && reportHistory.isDirectory()) {
            resultsHistory.getParentFile().mkdirs();
            FileUtils.copyDirectory(reportHistory, resultsHistory);
        }
    }

    /** Optional: after report generation, store history to a stable cache for next run. */
    public static void exportHistoryToCache() throws Exception {
        File reportHistory = new File(reportDir() + "/history");
        File cache = new File(System.getProperty("user.dir") + "/.allure-history");
        if (reportHistory.exists() && reportHistory.isDirectory()) {
            cache.mkdirs();
            FileUtils.copyDirectory(reportHistory, cache);
        }
    }

    /** Optional: inject from stable cache if no previous report exists. */
    public static void injectFromCacheIfAvailable() throws Exception {
        File cache = new File(System.getProperty("user.dir") + "/.allure-history");
        File resultsHistory = new File(resultsDir() + "/history");
        if (cache.exists() && cache.isDirectory()) {
            resultsHistory.getParentFile().mkdirs();
            FileUtils.copyDirectory(cache, resultsHistory);
        }
    }
}
