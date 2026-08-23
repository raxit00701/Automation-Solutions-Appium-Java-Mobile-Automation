package base;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import io.appium.java_client.android.AndroidDriver;
import utils.ResetPolicy;

public class MastodonBase {

    protected AndroidDriver driver() {
        return DriverFactory.getDriver();
    }

    /*
     * =========================================================
     * NORMAL TEST LIFECYCLE
     * =========================================================
     *
     * One Appium session per @Test method.
     *
     * IMPORTANT:
     * Test classes that need ONE session for multiple DataProvider
     * rows should NOT use this lifecycle. They should use
     * setUpClass() / tearDownClass().
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp(ITestResult testResult) throws Exception {

        ResetPolicy.Mode resetMode =
                resolveResetPolicy(testResult);

        initializeDriver(resetMode);
    }

    /*
     * =========================================================
     * CLASS-LEVEL LIFECYCLE
     * =========================================================
     *
     * Used by Test1 when all CSV rows should share one driver.
     */
    protected void setUpClass() throws Exception {

        Class<?> testClass = getClass();

        ResetPolicy.Mode resetMode =
                ResetPolicy.Mode.INHERIT;

        ResetPolicy classPolicy =
                testClass.getAnnotation(
                        ResetPolicy.class
                );

        if (classPolicy != null) {
            resetMode = classPolicy.value();
        }

        System.out.println(
                "[SETUP-CLASS] Reset Policy = "
                        + resetMode
        );

        initializeDriver(resetMode);
    }

    /*
     * =========================================================
     * DRIVER INITIALIZATION
     * =========================================================
     */
    private void initializeDriver(
            ResetPolicy.Mode resetMode
    ) throws Exception {

        String udid =
                System.getProperty(
                        "udid",
                        "emulator-5554"
                );

        int serverPort =
                Integer.parseInt(
                        System.getProperty(
                                "serverPort",
                                "4724"
                        )
                );

        int systemPort =
                Integer.parseInt(
                        System.getProperty(
                                "systemPort",
                                "8200"
                        )
                );

        int chromePort =
                Integer.parseInt(
                        System.getProperty(
                                "chromePort",
                                "9515"
                        )
                );

        System.out.printf(
                "[SETUP] Starting driver | " +
                "udid=%s | server=%d | system=%d | chrome=%d | reset=%s%n",
                udid,
                serverPort,
                systemPort,
                chromePort,
                resetMode
        );

        DriverFactory.initDriver(
                udid,
                serverPort,
                systemPort,
                chromePort,
                resetMode
        );
    }

    /*
     * =========================================================
     * RESET POLICY RESOLUTION
     * =========================================================
     *
     * Priority:
     *
     * METHOD annotation
     *       ↓
     * CLASS annotation
     *       ↓
     * INHERIT
     */
    private ResetPolicy.Mode resolveResetPolicy(
            ITestResult testResult
    ) {

        ResetPolicy.Mode resetMode =
                ResetPolicy.Mode.INHERIT;

        Class<?> testClass =
                testResult
                        .getTestClass()
                        .getRealClass();

        /*
         * Class-level policy
         */
        ResetPolicy classPolicy =
                testClass.getAnnotation(
                        ResetPolicy.class
                );

        if (classPolicy != null) {
            resetMode = classPolicy.value();
        }

        /*
         * Method-level policy overrides class-level policy.
         */
        if (testResult.getMethod()
                .getConstructorOrMethod()
                .getMethod() != null) {

            ResetPolicy methodPolicy =
                    testResult
                            .getMethod()
                            .getConstructorOrMethod()
                            .getMethod()
                            .getAnnotation(
                                    ResetPolicy.class
                            );

            if (methodPolicy != null) {
                resetMode = methodPolicy.value();
            }
        }

        return resetMode;
    }

    /*
     * =========================================================
     * NORMAL TEST TEARDOWN
     * =========================================================
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {

        DriverFactory.quitDriver();
    }

    /*
     * =========================================================
     * CLASS-LEVEL TEARDOWN
     * =========================================================
     *
     * Used by Test1.
     */
    protected void tearDownClass() {

        DriverFactory.quitDriver();
    }
}