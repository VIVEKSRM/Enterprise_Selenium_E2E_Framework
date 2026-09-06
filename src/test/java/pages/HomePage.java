package pages;

import core.base.BasePage;
import org.openqa.selenium.By;

public class HomePage extends BasePage {

    private final By logoutLink =
            By.cssSelector("a[href='/logout']");

    /*
     * Verifies whether the user is logged in.
     * The Logout link is displayed only after successful login.
     */
    public boolean isUserLoggedIn() {

        return wait.visible(logoutLink).isDisplayed();
    }

    /*
     * Logs out the currently logged-in user.
     * After logout, the application navigates to the Login page.
     */
    public LoginPage logout() {

        wait.clickable(logoutLink).click();

        return new LoginPage();
    }
}