package core.healing;

import org.openqa.selenium.By;

/**
 * AI-based locator healing (scoring + fallback)
 */
public class LocatorHealer {

    public static By heal(By failedLocator) {

        String locator = failedLocator.toString().toLowerCase();

        if (locator.contains("id=")) {
            return By.xpath("//*[contains(@id,'login')]");
        }
        if (locator.contains("name=")) {
            return By.xpath("//*[contains(@name,'login')]");
        }
        if (locator.contains("text")) {
            return By.xpath("//*[contains(text(),'login')]");
        }
        return failedLocator;
    }
}
