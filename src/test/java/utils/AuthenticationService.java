package utils;

import core.config.ConfigManager;
import pages.HomePage;
import pages.LoginPage;

/**
 * Centralizes application authentication operations.
 */
public class AuthenticationService {

    /**
     * Logs in using the default valid credentials from config.properties.
     *
     * @return HomePage after successful login
     */
    public HomePage login() {

        String username =
                getRequiredConfig("login.valid.email");

        String password =
                getRequiredConfig("login.valid.password");

        return login(username, password);
    }

    /**
     * Logs in using the supplied username and password.
     *
     * @param username username used for login
     * @param password password used for login
     * @return HomePage after successful login
     */
    public HomePage login(
            String username,
            String password) {

        validateCredential(username, "username");
        validateCredential(password, "password");

        return new LoginPage()
                .enterEmail(username)
                .enterPassword(password)
                .clickLogin();
    }

    /**
     * Attempts login using the configured invalid password.
     *
     * @return LoginPage after unsuccessful login attempt
     */
    public LoginPage loginWithInvalidPassword() {

        String username =
                getRequiredConfig("login.valid.email");

        String invalidPassword =
                getRequiredConfig("login.invalid.password");

        return loginWithInvalidPassword(
                username,
                invalidPassword
        );
    }

    /**
     * Attempts login using the supplied username and invalid password.
     *
     * @param username username used for login
     * @param invalidPassword invalid password
     * @return LoginPage after unsuccessful login attempt
     */
    public LoginPage loginWithInvalidPassword(
            String username,
            String invalidPassword) {

        validateCredential(username, "username");
        validateCredential(invalidPassword, "invalid password");

        return new LoginPage()
                .enterEmail(username)
                .enterPassword(invalidPassword)
                .submitLogin();
    }

    private String getRequiredConfig(String key) {

        String value = ConfigManager.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required configuration is missing: " + key
            );
        }

        return value;
    }

    private void validateCredential(
            String value,
            String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be null or blank"
            );
        }
    }
}