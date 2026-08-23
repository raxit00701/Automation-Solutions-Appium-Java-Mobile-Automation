package pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;

public class Checkoutflow {

    private AndroidDriver driver;
    private WebDriverWait wait;

    // ---------------------------------------------------------
    // PAGE ELEMENTS
    // ---------------------------------------------------------

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Sauce Labs Backpack\")")
    private WebElement backpack;

    @AndroidFindBy(uiAutomator = "new UiSelector().description(\"test-Inventory item page\")")
    private WebElement inventoryItemPage;

    // Appium automatically triggers the scroll when looking for this element
    @AndroidFindBy(uiAutomator = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(\"test-ADD TO CART\"))")
    private WebElement addToCartBtn;

    @AndroidFindBy(uiAutomator = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(\"Sauce Labs Backpack\"))")
    private WebElement backpackScrollUp;

    @AndroidFindBy(uiAutomator = "new UiSelector().className(\"android.widget.ImageView\").instance(3)")
    private WebElement cartIcon;

    // Locator to extract the text badge inside the Cart container
    @AndroidFindBy(xpath = "//android.view.ViewGroup[@content-desc=\"test-Cart\"]//android.widget.TextView")
    private List<WebElement> cartBadgeTexts;

    @AndroidFindBy(accessibility = "test-Item")
    private WebElement testItemContainer;

    @AndroidFindBy(uiAutomator = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(\"test-CHECKOUT\"))")
    private WebElement checkoutBtn;

    @AndroidFindBy(accessibility = "test-First Name")
    private WebElement firstNameField;

    @AndroidFindBy(accessibility = "test-Last Name")
    private WebElement lastNameField;

    @AndroidFindBy(accessibility = "test-Zip/Postal Code")
    private WebElement zipCodeField;

    @AndroidFindBy(uiAutomator = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(\"test-CONTINUE\"))")
    private WebElement continueBtn;

    @AndroidFindBy(uiAutomator = "new UiSelector().className(\"android.view.ViewGroup\").instance(17)")
    private WebElement viewGroup17;

    @AndroidFindBy(uiAutomator = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().description(\"test-FINISH\"))")
    private WebElement finishBtn;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"THANK YOU FOR YOU ORDER\")")
    private WebElement thankYouTitle;

    @AndroidFindBy(uiAutomator = "new UiSelector().text(\"Your order has been dispatched, and will arrive just as fast as the pony can get there!\")")
    private WebElement dispatchMessage;

    // ---------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------
    public Checkoutflow(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    // ---------------------------------------------------------
    // PAGE ACTIONS
    // ---------------------------------------------------------

    public void clickBackpack() {
        System.out.println("Clicking Sauce Labs Backpack...");
        wait.until(ExpectedConditions.elementToBeClickable(backpack)).click();
    }

    public void printInventoryItemContent() {
        wait.until(ExpectedConditions.visibilityOf(inventoryItemPage));
        System.out.println("Inventory Item Page Content: " + inventoryItemPage.getText());
    }

    public void scrollToAndClickAddToCart() {
        System.out.println("Scrolling down to ADD TO CART...");
        addToCartBtn.click();
    }

    public void scrollBackUpToBackpack() {
        System.out.println("Scrolling back up to Sauce Labs Backpack...");
        backpackScrollUp.isDisplayed(); // Just triggering the lookup forces the scroll
    }

    public void clickCartIcon() {
        System.out.println("Checking Cart Count...");
        
        // --- NEW LIGHT ASSERTIONS ---
        // 1. Verify the badge actually appeared
        Assert.assertFalse(cartBadgeTexts.isEmpty(), "Cart badge is missing! Item was not added.");
        
        // 2. Verify the count is exactly "1"
        String currentCount = cartBadgeTexts.get(0).getText();
        System.out.println("-> Cart Item Count: " + currentCount);
        Assert.assertEquals(currentCount, "1", "Cart badge count is incorrect!");
        // -----------------------------

        System.out.println("Clicking the cart icon...");
        wait.until(ExpectedConditions.elementToBeClickable(cartIcon)).click();
    }

    public void printCartTestItem() {
        System.out.println("Fetching test-Item content after adding to cart...");
        wait.until(ExpectedConditions.visibilityOf(testItemContainer));
        List<WebElement> texts = testItemContainer.findElements(AppiumBy.className("android.widget.TextView"));
        System.out.print("test-Item Content: ");
        for (WebElement node : texts) {
            System.out.print(node.getText() + " | ");
        }
        System.out.println();
    }

    public void scrollToAndClickCheckout() {
        System.out.println("Scrolling down to CHECKOUT...");
        checkoutBtn.click();
    }

    public void enterCheckoutDetails(String fName, String lName, String zip) {
        System.out.println("Entering checkout details...");
        wait.until(ExpectedConditions.elementToBeClickable(firstNameField)).click();
        firstNameField.sendKeys(fName);
        lastNameField.sendKeys(lName);
        zipCodeField.sendKeys(zip);
    }

    public void scrollToAndClickContinue() {
        System.out.println("Scrolling down to CONTINUE...");
        continueBtn.click();
    }

    public void printCheckoutContents() {
        System.out.println("Fetching contents after clicking CONTINUE...");
        wait.until(ExpectedConditions.visibilityOf(testItemContainer));
        List<WebElement> checkoutItemTexts = testItemContainer.findElements(AppiumBy.className("android.widget.TextView"));
        System.out.print("Checkout test-Item Content: ");
        for (WebElement node : checkoutItemTexts) {
            System.out.print(node.getText() + " | ");
        }
        System.out.println();

        wait.until(ExpectedConditions.visibilityOf(viewGroup17));
        List<WebElement> group17Texts = viewGroup17.findElements(AppiumBy.className("android.widget.TextView"));
        System.out.print("ViewGroup 17 Content: ");
        for (WebElement node : group17Texts) {
            System.out.print(node.getText() + " | ");
        }
        System.out.println();
    }

    public void scrollToAndClickFinish() {
        System.out.println("Scrolling down to FINISH...");
        finishBtn.click();
    }

    public void verifyOrderConfirmation() {
        System.out.println("Verifying order confirmation messages...");
        wait.until(ExpectedConditions.visibilityOf(thankYouTitle));
        Assert.assertTrue(thankYouTitle.isDisplayed(), "Thank You title is missing!");
        System.out.println("Assertion Passed: " + thankYouTitle.getText());

        wait.until(ExpectedConditions.visibilityOf(dispatchMessage));
        Assert.assertTrue(dispatchMessage.isDisplayed(), "Dispatch message is missing!");
        System.out.println("Assertion Passed: " + dispatchMessage.getText());
    }
}