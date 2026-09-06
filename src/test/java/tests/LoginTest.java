package tests;

import core.base.BaseTest;
import core.config.ConfigManager;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;

public class LoginTest extends BaseTest {

    private final String validEmail =ConfigManager.get("login.valid.email");
    private final String validPassword =ConfigManager.get("login.valid.password");
    private final String invalidPassword =ConfigManager.get("login.invalid.password");


    @Test(description = "Verify successful login with valid credentials",priority = 1)
    public void verifySuccessfulLogin() {
        LoginPage loginPage = new LoginPage();
        HomePage homePage = loginPage
                .enterEmail(validEmail)
                .enterPassword(validPassword)
                .clickLogin();

        Assert.assertTrue(
                homePage.isUserLoggedIn(),
                "User should be logged in successfully"
        );
    }

    @Test(description = "Verify login failure with invalid password",priority = 2)
    public void verifyLoginWithInvalidPassword() {

        LoginPage loginPage = new LoginPage();

        loginPage
                .enterEmail(validEmail)
                .enterPassword(invalidPassword)
                .submitLogin();

        Assert.assertTrue(
                loginPage.getLoginErrorMessage()
                        .toLowerCase()
                        .contains("unsuccessful"),
                "Expected login error message was not displayed"
        );
    }

    @Test(description = "Verify validation for blank email and password",priority = 3)
    public void verifyLoginWithBlankCredentials() {

        LoginPage loginPage = new LoginPage();

        loginPage.submitLogin();

        Assert.assertTrue(
                loginPage.getEmailValidationMessage()
                        .toLowerCase()
                        .contains("unsuccessful"),
                "Login was unsuccessful. Please correct the errors and try again."
        );

        Assert.assertTrue(
                loginPage.getPasswordValidationMessage()
                        .toLowerCase()
                        .contains("incorrect"),
                "The credentials provided are incorrect"
        );
    }

    @Test(description = "Verify logout after successful login",priority = 4)
    public void verifyLogout() {

        LoginPage loginPage = new LoginPage();

        HomePage homePage = loginPage
                .enterEmail(validEmail)
                .enterPassword(validPassword)
                .clickLogin();

        Assert.assertTrue(
                homePage.isUserLoggedIn(),
                "User should be logged in before logout"
        );

        LoginPage loginPageAfterLogout = homePage.logout();

        Assert.assertTrue(
                loginPageAfterLogout.isLoginPageDisplayed(),
                "Login page should be displayed after logout"
        );
    }
}