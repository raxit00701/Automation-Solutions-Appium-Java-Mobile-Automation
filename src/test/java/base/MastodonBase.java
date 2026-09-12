
package base;

import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import io.appium.java_client.android.AndroidDriver;
import utils.ResetPolicy;

public class MastodonBase {

    // ============================================================
    // DRIVER ACCESS
    // ============================================================

    protected AndroidDriver driver() {
        return DriverFactory.getDriver();
    }

    // ============================================================
    // BEFORE METHOD
    // ============================================================

    @BeforeMethod(alwaysRun = true)
    public void setUp(ITestContext context) {

        String environment =
                System.getProperty("env", "qa");

        String udid =
                getProperty(
                        "udid",
                        "emulator-5554"
                );

        int serverPort =
                getIntProperty(
                        "serverPort",
                        4724
                );

        int systemPort =
                getIntProperty(
                        "systemPort",
                        8200
                );

        int chromeDriverPort =
                getIntProperty(
                        "chromePort",
                        9515
                );

        ResetPolicy.Mode resetPolicy =
                resolveResetPolicy();

        System.out.println();
        System.out.println(
                "============================================================"
        );
        System.out.println(
                "[SETUP] Starting driver"
        );
        System.out.println(
                "============================================================"
        );

        System.out.println(
                "[SETUP] env          = " +
                        environment
        );

        System.out.println(
                "[SETUP] udid         = " +
                        udid
        );

        System.out.println(
                "[SETUP] serverPort   = " +
                        serverPort
        );

        System.out.println(
                "[SETUP] systemPort   = " +
                        systemPort
        );

        System.out.println(
                "[SETUP] chromePort   = " +
                        chromeDriverPort
        );

        System.out.println(
                "[SETUP] reset        = " +
                        resetPolicy
        );

        System.out.println(
                "============================================================"
        );

        initializeDriver(
                udid,
                serverPort,
                systemPort,
                chromeDriverPort,
                resetPolicy
        );
    }

    // ============================================================
    // INITIALIZE DRIVER
    // ============================================================

    protected void initializeDriver(
            String udid,
            int serverPort,
            int systemPort,
            int chromeDriverPort,
            ResetPolicy.Mode resetPolicy
    ) {

        DriverFactory.initDriver(
                udid,
                serverPort,
                systemPort,
                chromeDriverPort,
                resetPolicy
        );
    }

    // ============================================================
    // AFTER METHOD
    // ============================================================

    @AfterMethod(alwaysRun = true)
    public void tearDown() {

        System.out.println();
        System.out.println(
                "[TEARDOWN] Closing driver..."
        );

        DriverFactory.quitDriver();

        System.out.println(
                "[TEARDOWN] Driver closed."
        );
    }

    // ============================================================
    // SHARED CLASS DRIVER
    // ============================================================

    protected void setUpClass() {

        String environment =
                System.getProperty("env", "qa");

        String udid =
                getProperty(
                        "udid",
                        "emulator-5554"
                );

        int serverPort =
                getIntProperty(
                        "serverPort",
                        4724
                );

        int systemPort =
                getIntProperty(
                        "systemPort",
                        8200
                );

        int chromeDriverPort =
                getIntProperty(
                        "chromePort",
                        9515
                );

        ResetPolicy.Mode resetPolicy =
                resolveResetPolicy();

        System.out.println();
        System.out.println(
                "============================================================"
        );
        System.out.println(
                "[CLASS SETUP]"
        );
        System.out.println(
                "============================================================"
        );

        System.out.println(
                "[CLASS SETUP] env     = " +
                        environment
        );

        System.out.println(
                "[CLASS SETUP] udid    = " +
                        udid
        );

        System.out.println(
                "[CLASS SETUP] server  = " +
                        serverPort
        );

        System.out.println(
                "[CLASS SETUP] system  = " +
                        systemPort
        );

        System.out.println(
                "[CLASS SETUP] chrome  = " +
                        chromeDriverPort
        );

        System.out.println(
                "[CLASS SETUP] reset   = " +
                        resetPolicy
        );

        System.out.println(
                "============================================================"
        );

        initializeDriver(
                udid,
                serverPort,
                systemPort,
                chromeDriverPort,
                resetPolicy
        );
    }

    // ============================================================
    // SHARED CLASS DRIVER TEARDOWN
    // ============================================================

    protected void tearDownClass() {

        System.out.println(
                "[CLASS TEARDOWN] Closing shared driver..."
        );

        DriverFactory.quitDriver();

        System.out.println(
                "[CLASS TEARDOWN] Shared driver closed."
        );
    }

    // ============================================================
    // RESET POLICY RESOLUTION
    // ============================================================

    protected ResetPolicy.Mode resolveResetPolicy() {

        // --------------------------------------------------------
        // COMMAND-LINE RESET
        // --------------------------------------------------------

        String reset =
                System.getProperty(
                        "reset",
                        "NO_RESET"
                );

        if (reset == null || reset.isBlank()) {

            return ResetPolicy.Mode.NO_RESET;
        }

        try {

            return ResetPolicy.Mode.valueOf(
                    reset.trim().toUpperCase()
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid reset policy: " +
                            reset +
                            ". Valid values: " +
                            "NO_RESET, FAST_RESET, " +
                            "RESET_DATA, FULL_RESET, INHERIT"
            );
        }
    }

    // ============================================================
    // SYSTEM PROPERTY - STRING
    // ============================================================

    private String getProperty(
            String key,
            String defaultValue
    ) {

        String value =
                System.getProperty(key);

        if (value == null || value.isBlank()) {

            return defaultValue;
        }

        return value.trim();
    }

    // ============================================================
    // SYSTEM PROPERTY - INTEGER
    // ============================================================

    private int getIntProperty(
            String key,
            int defaultValue
    ) {

        String value =
                System.getProperty(key);

        if (value == null || value.isBlank()) {

            return defaultValue;
        }

        try {

            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "System property '" +
                            key +
                            "' must be an integer. " +
                            "Value: " +
                            value
            );
        }
    }
}
