package core.healing;

import core.driver.DriverManager;
import org.openqa.selenium.*;

/**
 * Self-healing WebDriver wrapper
 */
public class SelfHealingDriver {

    public static WebElement find(By locator) {
        try {
            return DriverManager.getDriver().findElement(locator);
        } catch (NoSuchElementException e) {
            By healed = LocatorHealer.heal(locator);
            return DriverManager.getDriver().findElement(healed);
        }
    }

    // Alias for common Selenium naming; some tests use findElement
    public static WebElement findElement(By locator) {
        return find(locator);
    }
}
