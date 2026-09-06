package tests;

import core.base.BaseTest;
import core.reporting.ExtentManager;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;
import utils.AuthenticationService;

/**
 * Contains end-to-end login-related test scenarios.
 */
public class LoginTest extends BaseTest {

    private final AuthenticationService authentication =new AuthenticationService();

    /**
     * Test 1: Verify successful login using valid credentials.
     */
    @Test(description = "Verify successful login with valid credentials",priority = 1)
    public void verifySuccessfulLogin() {

        ExtentManager.log("Starting successful login test");
        try {
            ExtentManager.log("Attempting login using valid credentials");
            HomePage homePage =authentication.login();
            ExtentManager.log("Valid credentials submitted successfully");

            boolean isLoggedIn =homePage.isUserLoggedIn();
            Assert.assertTrue(isLoggedIn,"User should be logged in successfully");

            ExtentManager.pass("User logged in successfully");

        } catch (AssertionError exception) {

            ExtentManager.fail("Successful login validation failed: "+ exception.getMessage());
            throw exception;

        } catch (Exception exception) {

            ExtentManager.fail("Successful login test failed: "+ exception.getMessage());
            throw exception;
        }
    }

    /**
     * Test 2: Verify login failure using an invalid password.
     */
    @Test(
            description = "Verify login failure with invalid password",
            priority = 2
    )
    public void verifyLoginWithInvalidPassword() {

        ExtentManager.log(
                "Starting invalid-password login test"
        );

        try {
            ExtentManager.log(
                    "Attempting login using an invalid password"
            );

            LoginPage loginPage =
                    authentication.loginWithInvalidPassword();

            String actualErrorMessage =
                    loginPage.getLoginErrorMessage();

            ExtentManager.log(
                    "Actual login error message: "
                            + actualErrorMessage
            );

            Assert.assertTrue(
                    actualErrorMessage
                            .toLowerCase()
                            .contains("unsuccessful"),
                    "Expected invalid login error message was not displayed. "
                            + "Actual message: " + actualErrorMessage
            );

            ExtentManager.pass(
                    "Invalid password was rejected successfully"
            );

        } catch (AssertionError exception) {

            ExtentManager.fail(
                    "Invalid-password validation failed: "
                            + exception.getMessage()
            );

            throw exception;

        } catch (Exception exception) {

            ExtentManager.fail(
                    "Invalid-password test failed: "
                            + exception.getMessage()
            );

            throw exception;
        }
    }

    /**
     * Test 3: Verify validation messages when email and password
     * are left blank.
     */
    @Test(
            description = "Verify validation for blank email and password",
            priority = 3
    )
    public void verifyLoginWithBlankCredentials() {

        ExtentManager.log(
                "Starting blank-credentials validation test"
        );

        try {
            LoginPage loginPage =
                    new LoginPage();

            ExtentManager.log(
                    "Submitting login form without entering credentials"
            );

            loginPage.submitLogin();

            String emailValidationMessage =
                    loginPage.getEmailValidationMessage();

            String passwordValidationMessage =
                    loginPage.getPasswordValidationMessage();

            ExtentManager.log(
                    "Email validation message: "
                            + emailValidationMessage
            );

            ExtentManager.log(
                    "Password validation message: "
                            + passwordValidationMessage
            );

            Assert.assertTrue(
                    emailValidationMessage
                            .toLowerCase()
                            .contains("email"),
                    "Email validation message was not displayed. "
                            + "Actual message: "
                            + emailValidationMessage
            );

            Assert.assertTrue(
                    passwordValidationMessage
                            .toLowerCase()
                            .contains("password"),
                    "Password validation message was not displayed. "
                            + "Actual message: "
                            + passwordValidationMessage
            );

            ExtentManager.pass(
                    "Validation messages were displayed for blank credentials"
            );

        } catch (AssertionError exception) {

            ExtentManager.fail(
                    "Blank-credentials validation failed: "
                            + exception.getMessage()
            );

            throw exception;

        } catch (Exception exception) {

            ExtentManager.fail(
                    "Blank-credentials test failed: "
                            + exception.getMessage()
            );

            throw exception;
        }
    }

    /**
     * Test 4: Verify that a successfully logged-in user can log out.
     */
    @Test(
            description = "Verify logout after successful login",
            priority = 4
    )
    public void verifyLogout() {

        ExtentManager.log(
                "Starting logout test"
        );

        try {
            ExtentManager.log(
                    "Logging in using valid credentials"
            );

            HomePage homePage =
                    authentication.login();

            Assert.assertTrue(
                    homePage.isUserLoggedIn(),
                    "User should be logged in before logout"
            );

            ExtentManager.log(
                    "User is logged in successfully"
            );

            ExtentManager.log(
                    "Clicking logout"
            );

            LoginPage loginPageAfterLogout =
                    homePage.logout();

            Assert.assertTrue(
                    loginPageAfterLogout.isLoginPageDisplayed(),
                    "Login page should be displayed after logout"
            );

            ExtentManager.pass(
                    "User logged out successfully and login page was displayed"
            );

        } catch (AssertionError exception) {

            ExtentManager.fail(
                    "Logout validation failed: "
                            + exception.getMessage()
            );

            throw exception;

        } catch (Exception exception) {

            ExtentManager.fail(
                    "Logout test failed: "
                            + exception.getMessage()
            );

            throw exception;
        }
    }
}