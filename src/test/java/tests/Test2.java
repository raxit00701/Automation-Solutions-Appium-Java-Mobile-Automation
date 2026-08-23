package tests;

import org.testng.annotations.Test;

import base.MastodonBase;
import pages.Checkoutflow;
import utils.ResetPolicy;
import static utils.ResetPolicy.Mode.NO_RESET;

@ResetPolicy(NO_RESET)
public class Test2 extends MastodonBase {

    @Test
    public void testCheckoutFlow() {
        // ---------------------------------------------------------
        // 1. LOGIN
        // ---------------------------------------------------------
        /*LoginPage loginPage = new LoginPage(driver());
        
        loginPage.enterUsername("standard_user");
        loginPage.enterPassword("secret_sauce");
        loginPage.clickLogin();*/

        // ---------------------------------------------------------
        // 2. INVENTORY & ADD TO CART
        // ---------------------------------------------------------
        Checkoutflow checkoutFlow = new Checkoutflow(driver());

        checkoutFlow.clickBackpack();
        checkoutFlow.printInventoryItemContent();
        checkoutFlow.scrollToAndClickAddToCart();
        
        // Scroll back to top before clicking the cart icon
        checkoutFlow.scrollBackUpToBackpack();
        
        
        // This will now check and print the cart count before clicking!
        checkoutFlow.clickCartIcon();
        
        // --- NEW PRINT STATEMENT (Cart Screen) ---
        checkoutFlow.printCartTestItem(); 
        // -----------------------------------------

        // ---------------------------------------------------------
        // 3. CART & CHECKOUT
        // ---------------------------------------------------------
        checkoutFlow.scrollToAndClickCheckout();
        
        checkoutFlow.enterCheckoutDetails("ram singh", "kumar", "320561");
        checkoutFlow.scrollToAndClickContinue();

        // --- NEW PRINT STATEMENTS (Overview Screen) ---
        checkoutFlow.printCheckoutContents();
        // ----------------------------------------------
        
        checkoutFlow.scrollToAndClickFinish();

        // ---------------------------------------------------------
        // 4. ASSERTIONS
        // ---------------------------------------------------------
        checkoutFlow.verifyOrderConfirmation();
    }
}