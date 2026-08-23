package tests;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import base.MastodonBase;
import pages.LoginPage;
import utils.CsvUtils;
import utils.ResetPolicy;
import static utils.ResetPolicy.Mode.NO_RESET;

@ResetPolicy(NO_RESET)
public class Test1 extends MastodonBase {

    @DataProvider(name = "loginCsvData")
    public Object[][] getLoginData() {
        return CsvUtils.readCsv("src/test/resources/data/Login.csv");
    }

    @Test(dataProvider = "loginCsvData")
    public void testLoginFixed(String username, String password, String expectedResult) {
        System.out.println("==================================================");
        System.out.println("Running Test - Username: [" + username + "] | Expected: [" + expectedResult + "]");

        // 1. Initialize the Page Object
        LoginPage loginPage = new LoginPage(driver());

        // 2. Perform Actions
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLogin();
        loginPage.waitForLoginResult();

        // 3. Evaluate results
        if (expectedResult.equalsIgnoreCase("Error")) {
            if (loginPage.isErrorDisplayed()) {
                System.out.println("RESULT: LOGIN FAILED (As Expected)");
            } else {
                Assert.fail("Expected an error message for negative test, but none was displayed.");
            }
        } else if (expectedResult.equalsIgnoreCase("Success")) {
            if (loginPage.isProductsPageDisplayed()) {
                System.out.println("RESULT: Login successful - 'PRODUCTS' displayed (As Expected).");
            } else {
                Assert.fail("Expected successful login, but PRODUCTS page was not displayed.");
            }
        }
        System.out.println("==================================================\n");
    }
}