package listeners;

import base.DriverFactory;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Allure;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.*;

import utils.AllureHistory;
import utils.MediaManager;
import utils.EnvWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class AllureTestListener implements ITestListener, ISuiteListener {

    private static final Logger log = LoggerFactory.getLogger("RUN");

    // ===== Suite-level =====
    @Override
    public void onStart(ISuite suite) {
        try {
            // try cache first, then normal report history injection
            AllureHistory.injectFromCacheIfAvailable(); // fallback cache
            AllureHistory.injectHistoryIfAvailable();    // normal: copy from last report
            log.info("Allure history injected.");
        } catch (Exception e) {
            log.warn("Allure history not injected: {}", e.getMessage());
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("Suite finished: {}", suite.getName());
    }

    // ===== Test-level =====
    @Override
    public void onTestStart(ITestResult result) {
        String testName = qualifiedName(result);
        AndroidDriver driver = DriverFactory.getDriver();

        // Resolve device key (deviceName if present else udid)
        String deviceKey = "unknown-device";
        if (driver != null) {
            String udid = String.valueOf(driver.getCapabilities().getCapability("udid"));
            String deviceName = String.valueOf(driver.getCapabilities().getCapability("deviceName"));
            deviceKey = (deviceName != null && !"null".equals(deviceName) && !deviceName.isBlank())
                    ? deviceName : (udid == null ? "unknown-device" : udid);
        }

        // Prepare folders per test/per device
        MediaManager.setDeviceKey(deviceKey);
        MediaManager.createPerTestFolders(testName);

        // Allure basic labels
        String author = System.getProperty("author", "Raxit");
        Allure.label("owner", author);
        Allure.label("severity", "high");

        if (driver != null) {
            // Minimal test context as parameters
            String udid = String.valueOf(driver.getCapabilities().getCapability("udid"));
            String appPkg = String.valueOf(driver.getCapabilities().getCapability("appPackage"));
            String platformName = String.valueOf(driver.getCapabilities().getCapability("platformName"));
            String platformVersion = String.valueOf(driver.getCapabilities().getCapability("platformVersion"));

            Allure.label("device", deviceKey);
            Allure.parameter("UDID", udid);
            Allure.parameter("Platform", platformName);
            Allure.parameter("Platform Version", platformVersion);
            Allure.parameter("App Package", appPkg);

            // --- Write environment only if changed (prevents duplicates) ---
            try {
                boolean written = EnvWriter.writeIfChanged(driver, author);
                if (written) {
                    log.info("[{}] environment.properties written/updated.", deviceKey);
                } else {
                    log.info("[{}] environment.properties up-to-date (skip duplicate).", deviceKey);
                }
            } catch (Exception e) {
                log.warn("[{}] Env write failed: {}", deviceKey, e.getMessage());
            }

            // Start screen recording (best-effort)
            try {
                driver.startRecordingScreen();
                log.info("[{}] Screen recording started.", deviceKey);
            } catch (Exception e) {
                log.warn("[{}] startRecordingScreen failed: {}", deviceKey, e.getMessage());
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        AndroidDriver driver = DriverFactory.getDriver();
        stopRecordingAndMaybeAttach(driver, result, /*attachOnFailureOnly*/ true);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        // Force FAILED instead of BROKEN for non-assert exceptions
        Throwable t = result.getThrowable();
        if (t != null && !(t instanceof AssertionError)) {
            AssertionError ae = new AssertionError(
                    "Non-assertion failure converted to FAILED: " +
                            t.getClass().getSimpleName() + " - " + String.valueOf(t.getMessage())
            );
            try { ae.initCause(t); } catch (Exception ignore) {}
            result.setThrowable(ae);
            result.setStatus(ITestResult.FAILURE);
        }

        AndroidDriver driver = DriverFactory.getDriver();

        // 1) Screenshot
        try {
            if (driver != null) {
                File dest = MediaManager.currentScreenshotPath("FAIL", result);
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                FileUtils.copyFile(src, dest);
                Allure.addAttachment("Failure Screenshot", "image/png",
                        FileUtils.openInputStream(dest), ".png");
                log.info("[{}] Failure screenshot saved: {}", MediaManager.getDeviceKey(), dest.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("[{}] Screenshot failed: {}", MediaManager.getDeviceKey(), e.getMessage());
        }

        // 2) Page source (best-effort; helps when locator is removed/changed)
        try {
            if (driver != null) {
                String xml = driver.getPageSource();
                Allure.addAttachment("Page Source (XML)", "text/xml", xml, ".xml");
            }
        } catch (Exception ignore) {}

        // 3) Screen recording (attach on failure)
        stopRecordingAndMaybeAttach(driver, result, /*attachOnFailureOnly*/ true);

        // 4) Execution log (test-run.log)
        attachRunLogIfPresent();

        // 5) Short, readable error summary (top of the test in Allure)
        try {
            String msg = (t == null) ? "No throwable"
                    : (t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage()));
            Allure.addAttachment("Failure Summary", "text/plain", msg);
        } catch (Exception ignore) {}
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        AndroidDriver driver = DriverFactory.getDriver();
        stopRecordingAndMaybeAttach(driver, result, /*attachOnFailureOnly*/ true);
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onTestFailedWithTimeout(ITestResult result) { onTestFailure(result); }

    // ===== Helpers =====

    private void stopRecordingAndMaybeAttach(AndroidDriver driver, ITestResult result, boolean attachOnFailureOnly) {
        String deviceKey = MediaManager.getDeviceKey();
        try {
            if (driver == null) return;
            String b64 = driver.stopRecordingScreen();
            if (b64 == null || b64.isEmpty()) return;

            boolean shouldAttach = !attachOnFailureOnly || result.getStatus() == ITestResult.FAILURE;
            if (!shouldAttach) {
                log.info("[{}] Recording discarded (status: {}): {}", deviceKey, statusName(result), result.getName());
                return;
            }

            File file = MediaManager.currentVideoPath(result);
            byte[] data = Base64.getDecoder().decode(b64);
            FileUtils.writeByteArrayToFile(file, data);

            Allure.addAttachment("Screen Recording", "video/mp4",
                    FileUtils.openInputStream(file), ".mp4");
            log.info("[{}] Recording saved: {}", deviceKey, file.getAbsolutePath());
        } catch (Exception e) {
            log.warn("[{}] stopRecordingScreen failed: {}", deviceKey, e.getMessage());
        }
    }

    private void attachRunLogIfPresent() {
        try {
            File runLog = new File(System.getProperty("user.dir") + "/logs/test-run.log");
            if (runLog.exists()) {
                Allure.addAttachment("Execution Log", "text/plain",
                        FileUtils.openInputStream(runLog), ".log");
                log.info("Run log attached: {}", runLog.getAbsolutePath());
            }
        } catch (Exception ignored) {}
    }

    private static String qualifiedName(ITestResult result) {
        String cls = result.getMethod().getTestClass().getName();
        String mtd = result.getMethod().getMethodName();
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        return cls + "#" + mtd + "_" + stamp + "_T" + Thread.currentThread().getId();
    }

    private static String statusName(ITestResult r) {
        switch (r.getStatus()) {
            case ITestResult.SUCCESS: return "SUCCESS";
            case ITestResult.FAILURE: return "FAILURE";
            case ITestResult.SKIP:    return "SKIP";
            default:                  return String.valueOf(r.getStatus());
        }
    }
}



//-----------------------------------------------------------------------------------------------------------
/*It checks the throwable:

AssertionError ⇒ kind = "ASSERTION_FAILED"

anything else ⇒ kind = "BROKEN"
@Override
public void onTestFailure(ITestResult result) {
    AndroidDriver driver = DriverFactory.getDriver();

    // Classify failure for Allure & logs
    final Throwable t = result.getThrowable();
    final boolean isAssertionFailure = (t instanceof AssertionError);
    final String kind = isAssertionFailure ? "ASSERTION_FAILED" : "BROKEN";

    // Optional: surface in Allure report
    try {
        Allure.parameter("Failure Kind", kind); // shows in Parameters tab
    } catch (Exception ignore) {}

    // ... (rest of your existing code)
}

 */