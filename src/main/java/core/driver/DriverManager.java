package core.driver;

import org.openqa.selenium.WebDriver;

/**
 * Thread-safe WebDriver manager (Singleton + ThreadLocal)
 */
public final class DriverManager {

    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    private DriverManager() {}

    public static WebDriver getDriver() {
        return driver.get();
    }

    // made public so callers outside the package (listeners/tests) can set the driver
    public static void setDriver(WebDriver webDriver) {
        driver.set(webDriver);
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}
