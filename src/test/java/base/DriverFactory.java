package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import io.appium.java_client.Setting;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import utils.ResetPolicy;

public class DriverFactory {

    private static final ThreadLocal<AndroidDriver> TL_DRIVER =
            new ThreadLocal<>();

    public static AndroidDriver getDriver() {
        return TL_DRIVER.get();
    }

    public static void quitDriver() {
        AndroidDriver d = TL_DRIVER.get();

        if (d != null) {
            try {
                d.quit();
            } catch (Exception ignored) {
            } finally {
                TL_DRIVER.remove();
            }
        }
    }

    public static void initDriver(
            String udid,
            int serverPort,
            int systemPort,
            int chromePort,
            ResetPolicy.Mode resetMode
    ) throws MalformedURLException {

        /*
         * ---------------------------------------------------------
         * App under test
         * ---------------------------------------------------------
         */
        String appPackage = System.getProperty(
                "appPackage",
                "com.swaglabsmobileapp"
        );

        String appActivity = System.getProperty(
                "appActivity",
                ".SplashActivity"
        );

        /*
         * ---------------------------------------------------------
         * Apply reset policy BEFORE starting Appium session
         * ---------------------------------------------------------
         */
        switch (resetMode) {

            case NO_RESET:

                System.out.println(
                        "[RESET] NO_RESET -> preserving app data"
                );

                break;

            case RESET_DATA:

                System.out.println(
                        "[RESET] RESET_DATA -> clearing app data"
                );

                clearAppData(udid, appPackage);

                break;

            case FAST_RESET:

                System.out.println(
                        "[RESET] FAST_RESET -> clearing app data"
                );

                clearAppData(udid, appPackage);

                break;

            case FULL_RESET:

                System.out.println(
                        "[RESET] FULL_RESET -> uninstalling app"
                );

                uninstallApp(udid, appPackage);

                break;

            case INHERIT:
            default:

                boolean noReset =
                        Boolean.parseBoolean(
                                System.getProperty(
                                        "noReset",
                                        "true"
                                )
                        );

                boolean fullReset =
                        Boolean.parseBoolean(
                                System.getProperty(
                                        "fullReset",
                                        "false"
                                )
                        );

                if (fullReset) {

                    System.out.println(
                            "[RESET] INHERIT -> FULL_RESET"
                    );

                    uninstallApp(
                            udid,
                            appPackage
                    );

                } else if (!noReset) {

                    System.out.println(
                            "[RESET] INHERIT -> RESET_DATA"
                    );

                    clearAppData(
                            udid,
                            appPackage
                    );

                } else {

                    System.out.println(
                            "[RESET] INHERIT -> NO_RESET"
                    );
                }

                break;
        }

        /*
         * ---------------------------------------------------------
         * Create Appium options
         * ---------------------------------------------------------
         */
        UiAutomator2Options options =
                new UiAutomator2Options();

        // Device
        options.setDeviceName(
                (udid == null || udid.isBlank())
                        ? "Android"
                        : udid
        );

        if (udid != null && !udid.isBlank()) {
            options.setUdid(udid);
        }

        // App
        options.setAppPackage(appPackage);
        options.setAppActivity(appActivity);
        options.setAppWaitActivity("*");

        // Timeouts
        options.setAdbExecTimeout(
                Duration.ofSeconds(60)
        );

        options.setNewCommandTimeout(
                Duration.ofSeconds(300)
        );

        // QoL
        options.setCapability(
                "appWaitForQuiescence",
                false
        );

        options.setCapability(
                "disableWindowAnimation",
                true
        );

        options.setCapability(
                "autoGrantPermissions",
                true
        );

        options.setCapability(
                "unicodeKeyboard",
                true
        );

        options.setCapability(
                "resetKeyboard",
                true
        );

        // Parallel ports
        options.setCapability(
                "systemPort",
                systemPort
        );

        options.setCapability(
                "chromedriverPort",
                chromePort
        );

        /*
         * ---------------------------------------------------------
         * IMPORTANT
         *
         * Reset has already been handled above.
         *
         * Therefore Appium must NOT reset the app again.
         * ---------------------------------------------------------
         */
        options.setNoReset(true);
        options.setFullReset(false);

        // ChromeDriver
        String chromeExec =
                System.getProperty(
                        "chromedriverExecutable",
                        "C:\\chromedriver.exe"
                );

        options.setCapability(
                "chromedriverExecutable",
                chromeExec
        );

        /*
         * ---------------------------------------------------------
         * Start Appium session
         * ---------------------------------------------------------
         */
        String serverUrl =
                "http://127.0.0.1:" + serverPort;

        System.out.printf(
                "[DRIVER-FACTORY] " +
                "Starting driver: " +
                "udid=%s server=%d system=%d chrome=%d " +
                "reset=%s noReset=true fullReset=false " +
                "app=%s/%s%n",
                udid,
                serverPort,
                systemPort,
                chromePort,
                resetMode,
                appPackage,
                appActivity
        );

        AndroidDriver d;

        try {

            d = new AndroidDriver(
                    new URL(serverUrl),
                    options
            );

        } catch (
                org.openqa.selenium.SessionNotCreatedException e
        ) {

            String msg =
                    String.valueOf(e.getMessage());

            if (msg.contains("local port")
                    && msg.contains("busy")) {

                int newSystemPort =
                        systemPort + 1;

                int newChromePort =
                        chromePort + 2;

                options.setCapability(
                        "systemPort",
                        newSystemPort
                );

                options.setCapability(
                        "chromedriverPort",
                        newChromePort
                );

                System.out.printf(
                        "[DRIVER-FACTORY][RETRY] " +
                        "systemPort %d busy -> " +
                        "retry systemPort=%d " +
                        "chromePort=%d%n",
                        systemPort,
                        newSystemPort,
                        newChromePort
                );

                d = new AndroidDriver(
                        new URL(serverUrl),
                        options
                );

            } else {
                throw e;
            }
        }

        TL_DRIVER.set(d);

        /*
         * Faster element readiness
         */
        try {

            d.setSetting(
                    Setting.WAIT_FOR_IDLE_TIMEOUT,
                    0
            );

        } catch (Exception ignored) {
        }

        System.out.printf(
                "[DRIVER-FACTORY] " +
                "Driver started successfully: " +
                "reset=%s app=%s/%s%n",
                resetMode,
                appPackage,
                appActivity
        );
    }

    /*
     * =========================================================
     * RESET HELPERS
     * =========================================================
     */

    private static void clearAppData(
            String udid,
            String appPackage
    ) {

        String device =
                (udid == null || udid.isBlank())
                        ? "emulator-5554"
                        : udid;

        executeAdb(
                device,
                "shell",
                "pm",
                "clear",
                appPackage
        );

        System.out.println(
                "[RESET] App data cleared: "
                        + appPackage
        );
    }

    private static void uninstallApp(
            String udid,
            String appPackage
    ) {

        String device =
                (udid == null || udid.isBlank())
                        ? "emulator-5554"
                        : udid;

        executeAdb(
                device,
                "uninstall",
                appPackage
        );

        System.out.println(
                "[RESET] App uninstalled: "
                        + appPackage
        );
    }

    private static void executeAdb(
            String udid,
            String... arguments
    ) {

        try {

            String adb =
                    System.getProperty(
                            "adbPath",
                            "adb"
                    );

            String[] command =
                    new String[arguments.length + 3];

            command[0] = adb;
            command[1] = "-s";
            command[2] = udid;

            System.arraycopy(
                    arguments,
                    0,
                    command,
                    3,
                    arguments.length
            );

            System.out.println(
                    "[ADB] "
                            + String.join(
                                    " ",
                                    command
                            )
            );

            Process process =
                    new ProcessBuilder(command)
                            .redirectErrorStream(true)
                            .start();

            StringBuilder output =
                    new StringBuilder();

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            process.getInputStream()
                                    )
                            )
            ) {

                String line;

                while ((line = reader.readLine()) != null) {

                    output.append(line)
                            .append(System.lineSeparator());
                }
            }

            int exitCode =
                    process.waitFor();

            System.out.print(
                    output.toString()
            );

            if (exitCode != 0) {

                throw new RuntimeException(
                        "ADB command failed. " +
                        "Exit code=" + exitCode +
                        "\nOutput=" + output
                );
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to execute ADB command",
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            throw new RuntimeException(
                    "ADB command interrupted",
                    e
            );
        }
    }
}