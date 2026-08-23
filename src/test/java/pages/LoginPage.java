package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginPage {

    private AndroidDriver driver;
    private WebDriverWait wait;

    // ---------------------------------------------------------
    // PAGE ELEMENTS (Using AppiumFieldDecorator)
    // ---------------------------------------------------------
    
    @AndroidFindBy(accessibility = "test-Username")
    private WebElement usernameField;

    @AndroidFindBy(accessibility = "test-Password")
    private WebElement passwordField;

    @AndroidFindBy(accessibility = "test-LOGIN")
    private WebElement loginButton;

    // Using List<WebElement> for validation elements so we can safely check .isEmpty() without crashes
    @AndroidFindBy(accessibility = "test-Error message")
    private List<WebElement> errorContainers;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Password is required\")")
    private List<WebElement> passwordErrors;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Username is required\")")
    private List<WebElement> usernameErrors;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"PRODUCTS\")")
    private List<WebElement> productsHeaders;

    // ---------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------
    public LoginPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // Initialize the elements defined above with Appium's custom decorator
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    // ---------------------------------------------------------
    // PAGE ACTIONS
    // ---------------------------------------------------------

    public void enterUsername(String username) {
        System.out.println("Clicking username field...");
        wait.until(ExpectedConditions.elementToBeClickable(usernameField)).click();
        usernameField.clear();
        if (username != null && !username.isEmpty()) {
            System.out.println("Entering username: " + username);
            usernameField.sendKeys(username);
        }
    }

    public void enterPassword(String password) {
        System.out.println("Clicking password field...");
        wait.until(ExpectedConditions.elementToBeClickable(passwordField)).click();
        passwordField.clear();
        if (password != null && !password.isEmpty()) {
            System.out.println("Entering password: " + password);
            passwordField.sendKeys(password);
        }
    }

    public void clickLogin() {
        System.out.println("Clicking LOGIN button...");
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    /** Wait for either the error container or the products page to load. */
    public void waitForLoginResult() {
        System.out.println("Waiting for login result...");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(AppiumBy.androidUIAutomator("new UiSelector().text(\"PRODUCTS\")")),
                ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId("test-Error message"))
        ));
    }

    // ---------------------------------------------------------
    // PAGE VALIDATIONS
    // ---------------------------------------------------------

    public boolean isErrorDisplayed() {
        return !errorContainers.isEmpty() || !passwordErrors.isEmpty() || !usernameErrors.isEmpty();
    }

    public boolean isProductsPageDisplayed() {
        return !productsHeaders.isEmpty();
    }
}