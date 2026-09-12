
package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.github.cdimascio.dotenv.Dotenv;
import utils.ResetPolicy;

public final class DriverFactory {

    private static final ThreadLocal<AndroidDriver> DRIVER =
            new ThreadLocal<>();

    private DriverFactory() {
        // Utility class
    }

    // ============================================================
    // DRIVER ACCESS
    // ============================================================

    public static AndroidDriver getDriver() {

        AndroidDriver driver = DRIVER.get();

        if (driver == null) {
            throw new IllegalStateException(
                    "Driver has not been initialized for the current thread."
            );
        }

        return driver;
    }

    public static boolean isDriverInitialized() {
        return DRIVER.get() != null;
    }

    // ============================================================
    // INITIALIZE DRIVER
    // ============================================================

    public static void initDriver(
            String udid,
            int serverPort,
            int systemPort,
            int chromeDriverPort,
            ResetPolicy.Mode resetPolicy
    ) {

        String environment =
                System.getProperty("env", "qa")
                        .trim()
                        .toLowerCase();

        Dotenv dotenv = loadEnvironment(environment);

        // --------------------------------------------------------
        // APP CONFIGURATION
        // --------------------------------------------------------

        String appPackage =
                getRequired(dotenv, "APP_PACKAGE");

        String appActivity =
                getRequired(dotenv, "APP_ACTIVITY");

        String appWaitActivity =
                get(dotenv, "APP_WAIT_ACTIVITY", "*");

        // --------------------------------------------------------
        // PLATFORM CONFIGURATION
        // --------------------------------------------------------

        String platformName =
                get(dotenv, "PLATFORM_NAME", "Android");

        String automationName =
                get(dotenv, "AUTOMATION_NAME", "UiAutomator2");

        String deviceName =
                get(dotenv, "DEVICE_NAME", "Android");

        // --------------------------------------------------------
        // ADB CONFIGURATION
        // --------------------------------------------------------

        String adbPath =
                get(dotenv, "ADB_PATH", "adb");

        int adbExecTimeout =
                getInt(dotenv, "ADB_EXEC_TIMEOUT", 60);

        // --------------------------------------------------------
        // APPIUM TIMEOUTS
        // --------------------------------------------------------

        int newCommandTimeout =
                getInt(dotenv, "NEW_COMMAND_TIMEOUT", 300);

        int waitForIdleTimeout =
                getInt(dotenv, "WAIT_FOR_IDLE_TIMEOUT", 0);

        // --------------------------------------------------------
        // APPIUM BEHAVIOR
        // --------------------------------------------------------

        boolean appWaitForQuiescence =
                getBoolean(
                        dotenv,
                        "APP_WAIT_FOR_QUIESCENCE",
                        false
                );

        boolean disableWindowAnimation =
                getBoolean(
                        dotenv,
                        "DISABLE_WINDOW_ANIMATION",
                        true
                );

        boolean autoGrantPermissions =
                getBoolean(
                        dotenv,
                        "AUTO_GRANT_PERMISSIONS",
                        true
                );

        boolean unicodeKeyboard =
                getBoolean(
                        dotenv,
                        "UNICODE_KEYBOARD",
                        true
                );

        boolean resetKeyboard =
                getBoolean(
                        dotenv,
                        "RESET_KEYBOARD",
                        true
                );

        // --------------------------------------------------------
        // CHROMEDRIVER
        // --------------------------------------------------------

        String chromedriverExecutable =
                get(
                        dotenv,
                        "CHROMEDRIVER_EXECUTABLE",
                        ""
                );

        // --------------------------------------------------------
        // DEFAULT RESET POLICY
        // --------------------------------------------------------

        if (resetPolicy == null) {
            resetPolicy = ResetPolicy.Mode.NO_RESET;
        }

        // --------------------------------------------------------
        // LOG CONFIGURATION
        // --------------------------------------------------------

        System.out.println();
        System.out.println(
                "============================================================"
        );
        System.out.println(
                "[DRIVER-FACTORY] Starting driver"
        );
        System.out.println(
                "============================================================"
        );

        System.out.println(
                "[ENV] environment       = " + environment
        );

        System.out.println(
                "[ENV] appPackage        = " + appPackage
        );

        System.out.println(
                "[ENV] appActivity       = " + appActivity
        );

        System.out.println(
                "[ENV] platformName      = " + platformName
        );

        System.out.println(
                "[ENV] automationName    = " + automationName
        );

        System.out.println(
                "[ENV] deviceName        = " + deviceName
        );

        System.out.println(
                "[ENV] adbPath           = " + adbPath
        );

        System.out.println(
                "[EXEC] udid             = " + udid
        );

        System.out.println(
                "[EXEC] serverPort       = " + serverPort
        );

        System.out.println(
                "[EXEC] systemPort       = " + systemPort
        );

        System.out.println(
                "[EXEC] chromePort       = " + chromeDriverPort
        );

        System.out.println(
                "[RESET] policy          = " + resetPolicy
        );

        System.out.println(
                "============================================================"
        );

        // --------------------------------------------------------
        // VALIDATION
        // --------------------------------------------------------

        if (udid == null || udid.isBlank()) {
            throw new IllegalArgumentException(
                    "UDID is required."
            );
        }

        if (serverPort <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Appium server port: " +
                            serverPort
            );
        }

        if (systemPort <= 0) {
            throw new IllegalArgumentException(
                    "Invalid system port: " +
                            systemPort
            );
        }

        // --------------------------------------------------------
        // APPLY RESET POLICY
        // --------------------------------------------------------

        applyResetPolicy(
                resetPolicy,
                adbPath,
                udid,
                appPackage
        );

        // --------------------------------------------------------
        // APPIUM OPTIONS
        // --------------------------------------------------------

        UiAutomator2Options options =
                new UiAutomator2Options();

        // Basic capabilities
        options.setPlatformName(platformName);
        options.setAutomationName(automationName);
        options.setDeviceName(deviceName);
        options.setUdid(udid);

        // App capabilities
        options.setAppPackage(appPackage);
        options.setAppActivity(appActivity);
        options.setAppWaitActivity(appWaitActivity);

        // Timeouts
        options.setAdbExecTimeout(
                Duration.ofSeconds(adbExecTimeout)
        );

        options.setNewCommandTimeout(
                Duration.ofSeconds(newCommandTimeout)
        );

        // --------------------------------------------------------
        // CAPABILITIES THAT ARE NOT EXPOSED AS JAVA-CLIENT
        // 9.2.2 TYPED SETTERS
        // --------------------------------------------------------

        options.setCapability(
                "appium:appWaitForQuiescence",
                appWaitForQuiescence
        );

        options.setCapability(
                "appium:unicodeKeyboard",
                unicodeKeyboard
        );

        options.setCapability(
                "appium:resetKeyboard",
                resetKeyboard
        );

        // --------------------------------------------------------
        // OTHER APPIUM CAPABILITIES
        // --------------------------------------------------------

        options.setDisableWindowAnimation(
                disableWindowAnimation
        );

        options.setAutoGrantPermissions(
                autoGrantPermissions
        );

        // --------------------------------------------------------
        // SYSTEM PORT
        // --------------------------------------------------------

        options.setSystemPort(systemPort);

        // --------------------------------------------------------
        // CHROMEDRIVER PORT
        // --------------------------------------------------------

        if (chromeDriverPort > 0) {

            options.setChromedriverPort(
                    chromeDriverPort
            );
        }

        // --------------------------------------------------------
        // CHROMEDRIVER EXECUTABLE
        // --------------------------------------------------------

        if (!chromedriverExecutable.isBlank()) {

            options.setChromedriverExecutable(
                    chromedriverExecutable
            );
        }

        // --------------------------------------------------------
        // RESET CAPABILITIES
        // --------------------------------------------------------

        applyResetCapabilities(
                options,
                resetPolicy
        );

        // --------------------------------------------------------
        // APPIUM SERVER
        // --------------------------------------------------------

        String serverUrl =
                "http://127.0.0.1:" + serverPort;

        System.out.println();
        System.out.println(
                "[DRIVER] Creating AndroidDriver..."
        );

        System.out.println(
                "[DRIVER] Appium server = " +
                        serverUrl
        );

        // --------------------------------------------------------
        // CREATE DRIVER
        // --------------------------------------------------------

        try {

            AndroidDriver driver =
                    new AndroidDriver(
                            URI.create(serverUrl).toURL(),
                            options
                    );

            DRIVER.set(driver);

            // ----------------------------------------------------
            // WAIT FOR IDLE TIMEOUT
            // ----------------------------------------------------

            if (waitForIdleTimeout >= 0) {

                try {

                    driver.setSetting(
                            "waitForIdleTimeout",
                            waitForIdleTimeout
                    );

                } catch (Exception e) {

                    System.out.println(
                            "[WARN] Could not set " +
                                    "waitForIdleTimeout: " +
                                    e.getMessage()
                    );
                }
            }

            System.out.println();
            System.out.println(
                    "[DRIVER] Driver initialized successfully."
            );

            System.out.println(
                    "[DRIVER] Session ID = " +
                            driver.getSessionId()
            );

            System.out.println();

        } catch (Exception e) {

            DRIVER.remove();

            System.err.println();
            System.err.println(
                    "============================================================"
            );

            System.err.println(
                    "[DRIVER-ERROR] Failed to create AndroidDriver"
            );

            System.err.println(
                    "============================================================"
            );

            System.err.println(
                    "[DRIVER-ERROR] Appium server = " +
                            serverUrl
            );

            System.err.println(
                    "[DRIVER-ERROR] UDID            = " +
                            udid
            );

            System.err.println(
                    "[DRIVER-ERROR] systemPort      = " +
                            systemPort
            );

            System.err.println(
                    "[DRIVER-ERROR] chromePort      = " +
                            chromeDriverPort
            );

            System.err.println(
                    "[DRIVER-ERROR] Cause           = " +
                            e.getMessage()
            );

            System.err.println(
                    "============================================================"
            );

            throw new RuntimeException(
                    "Unable to create AndroidDriver. " +
                            "Make sure Appium is running on port " +
                            serverPort + ".",
                    e
            );
        }
    }

    // ============================================================
    // RESET POLICY
    // ============================================================

    private static void applyResetPolicy(
            ResetPolicy.Mode resetPolicy,
            String adbPath,
            String udid,
            String appPackage
    ) {

        System.out.println(
                "[RESET] Applying policy: " +
                        resetPolicy
        );

        switch (resetPolicy) {

            case NO_RESET:

                System.out.println(
                        "[RESET] NO_RESET -> preserving app data"
                );

                break;

            case FAST_RESET:

                System.out.println(
                        "[RESET] FAST_RESET -> force-stopping app"
                );

                forceStopApp(
                        adbPath,
                        udid,
                        appPackage
                );

                break;

            case RESET_DATA:

                System.out.println(
                        "[RESET] RESET_DATA -> clearing app data"
                );

                clearAppData(
                        adbPath,
                        udid,
                        appPackage
                );

                break;

            case FULL_RESET:

                System.out.println(
                        "[RESET] FULL_RESET -> clearing app data"
                );

                clearAppData(
                        adbPath,
                        udid,
                        appPackage
                );

                break;

            case INHERIT:

                System.out.println(
                        "[RESET] INHERIT -> preserving app data"
                );

                break;

            default:

                System.out.println(
                        "[RESET] Unknown policy -> " +
                                "preserving app data"
                );

                break;
        }
    }

    // ============================================================
    // RESET CAPABILITIES
    // ============================================================

    private static void applyResetCapabilities(
            UiAutomator2Options options,
            ResetPolicy.Mode resetPolicy
    ) {

        switch (resetPolicy) {

            case NO_RESET:

                options.setNoReset(true);
                options.setFullReset(false);

                break;

            case FAST_RESET:

                options.setNoReset(true);
                options.setFullReset(false);

                break;

            case RESET_DATA:

                options.setNoReset(true);
                options.setFullReset(false);

                break;

            case FULL_RESET:

                options.setNoReset(false);
                options.setFullReset(true);

                break;

            case INHERIT:

                options.setNoReset(true);
                options.setFullReset(false);

                break;

            default:

                options.setNoReset(true);
                options.setFullReset(false);

                break;
        }
    }

    // ============================================================
    // ADB - FORCE STOP
    // ============================================================

    private static void forceStopApp(
            String adbPath,
            String udid,
            String appPackage
    ) {

        executeAdb(
                adbPath,
                udid,
                "shell",
                "am",
                "force-stop",
                appPackage
        );
    }

    // ============================================================
    // ADB - CLEAR DATA
    // ============================================================

    private static void clearAppData(
            String adbPath,
            String udid,
            String appPackage
    ) {

        executeAdb(
                adbPath,
                udid,
                "shell",
                "pm",
                "clear",
                appPackage
        );
    }

    // ============================================================
    // EXECUTE ADB
    // ============================================================

    private static List<String> executeAdb(
            String adbPath,
            String udid,
            String... arguments
    ) {

        List<String> command =
                new ArrayList<>();

        command.add(adbPath);
        command.add("-s");
        command.add(udid);

        for (String argument : arguments) {
            command.add(argument);
        }

        System.out.println(
                "[ADB] " +
                        String.join(" ", command)
        );

        ProcessBuilder processBuilder =
                new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);

        try {

            Process process =
                    processBuilder.start();

            List<String> output =
                    new ArrayList<>();

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            process.getInputStream()
                                    )
                            )
            ) {

                String line;

                while (
                        (line = reader.readLine()) != null
                ) {

                    output.add(line);

                    System.out.println(
                            "[ADB] " + line
                    );
                }
            }

            int exitCode =
                    process.waitFor();

            if (exitCode != 0) {

                throw new RuntimeException(
                        "ADB command failed with exit code " +
                                exitCode +
                                ": " +
                                String.join(
                                        " ",
                                        command
                                )
                );
            }

            return output;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not execute ADB command: " +
                            String.join(
                                    " ",
                                    command
                            ),
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "ADB command was interrupted.",
                    e
            );
        }
    }

    // ============================================================
    // QUIT DRIVER
    // ============================================================

    public static void quitDriver() {

        AndroidDriver driver =
                DRIVER.get();

        if (driver == null) {
            return;
        }

        try {

            System.out.println(
                    "[DRIVER] Quitting driver..."
            );

            driver.quit();

            System.out.println(
                    "[DRIVER] Driver quit successfully."
            );

        } catch (Exception e) {

            System.err.println(
                    "[DRIVER-WARN] Error while quitting driver: " +
                            e.getMessage()
            );

        } finally {

            DRIVER.remove();
        }
    }

    // ============================================================
    // ENVIRONMENT LOADING
    // ============================================================

    private static Dotenv loadEnvironment(
            String environment
    ) {

        String fileName =
                ".env." + environment;

        try {

            Dotenv dotenv =
                    Dotenv.configure()
                            .directory("./env")
                            .filename(fileName)
                            .load();

            System.out.println(
                    "[ENV] Loaded environment: " +
                            environment +
                            " (" +
                            fileName +
                            ")"
            );

            return dotenv;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not load environment file: " +
                            "env/" + fileName,
                    e
            );
        }
    }

    // ============================================================
    // ENV STRING
    // ============================================================

    private static String get(
            Dotenv dotenv,
            String key,
            String defaultValue
    ) {

        String value =
                dotenv.get(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    // ============================================================
    // ENV REQUIRED STRING
    // ============================================================

    private static String getRequired(
            Dotenv dotenv,
            String key
    ) {

        String value =
                dotenv.get(key);

        if (value == null || value.isBlank()) {

            throw new IllegalStateException(
                    "Required environment variable '" +
                            key +
                            "' is missing."
            );
        }

        return value.trim();
    }

    // ============================================================
    // ENV INTEGER
    // ============================================================

    private static int getInt(
            Dotenv dotenv,
            String key,
            int defaultValue
    ) {

        String value =
                dotenv.get(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {

            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Environment variable '" +
                            key +
                            "' must be an integer. " +
                            "Value: " +
                            value
            );
        }
    }

    // ============================================================
    // ENV BOOLEAN
    // ============================================================

    private static boolean getBoolean(
            Dotenv dotenv,
            String key,
            boolean defaultValue
    ) {

        String value =
                dotenv.get(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return Boolean.parseBoolean(
                value.trim()
        );
    }
}
