package pages;

import core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LoginPage extends BasePage {

    // Page locators
    private final By emailInput =
            By.id("Email");

    private final By passwordInput =
            By.id("Password");

    private final By loginButton =
            By.cssSelector("input[value='Log in']");

    private final By loginErrorMessage =
            By.cssSelector(".validation-summary-errors");

    private final By emailValidationMessage =
            By.cssSelector("//div[@class='validation-summary-errors']/span");

    private final By passwordValidationMessage =
            By.cssSelector("//div[@class='validation-summary-errors']/ul/li");

    private final By loginLink =
            By.cssSelector("a[href='/login']");
    /*
     * Enters the email address into the email field.
     */
    public LoginPage enterEmail(String email) {

        WebElement emailElement = wait.visible(emailInput);

        emailElement.clear();
        emailElement.sendKeys(email);

        return this;
    }

    /*
     * Enters the password into the password field.
     */
    public LoginPage enterPassword(String password) {

        WebElement passwordElement = wait.visible(passwordInput);

        passwordElement.clear();
        passwordElement.sendKeys(password);

        return this;
    }

    /*
     * Clicks the Login button after valid credentials are entered.
     * A successful login navigates to HomePage.
     */
    public HomePage clickLogin() {

        wait.clickable(loginButton).click();

        return new HomePage();
    }

    /*
     * Clicks the Login button for negative test scenarios.
     * The user is expected to remain on the LoginPage.
     */
    public LoginPage submitLogin() {

        wait.clickable(loginButton).click();

        return this;
    }

    /*
     * Returns the general login error message.
     */
    public String getLoginErrorMessage() {

        return wait.visible(loginErrorMessage).getText();
    }

    /*
     * Returns the validation message displayed for the email field.
     */
    public String getEmailValidationMessage() {

        return wait.visible(emailValidationMessage).getText();
    }

    /*
     * Returns the validation message displayed for the password field.
     */
    public String getPasswordValidationMessage() {

        return wait.visible(passwordValidationMessage).getText();
    }

    /*
     * Checks whether the Login page is displayed.
     */
    public boolean isDisplayed() {

        return wait.visible(emailInput).isDisplayed();
    }
    public boolean isLoginPageDisplayed() {
        return wait.visible(loginLink).isDisplayed();
    }
}