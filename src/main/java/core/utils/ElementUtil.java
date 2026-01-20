package core.utils;

import core.driver.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

/**
 * Enterprise-grade element utilities
 */
public class ElementUtil {

    private static final int TIMEOUT = 15;

    public static WebElement waitForVisible(By locator) {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(TIMEOUT))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(By locator) {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(TIMEOUT))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement fluentWait(By locator, int timeout, int polling) {
        Wait<WebDriver> wait = new FluentWait<>(DriverManager.getDriver())
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(polling))
                .ignoring(NoSuchElementException.class);
        return wait.until(d -> d.findElement(locator));
    }

    public static WebElement waitWithRefresh(By locator, int refreshCount) {
        WebDriver driver = DriverManager.getDriver();
        for (int i = 0; i < refreshCount; i++) {
            try {
                return waitForVisible(locator);
            } catch (TimeoutException e) {
                driver.navigate().refresh();
            }
        }
        throw new TimeoutException("Element not found after refresh");
    }

    public static void click(By locator) {
        waitForClickable(locator).click();
    }

    public static void sendKeys(By locator, String value) {
        WebElement el = waitForVisible(locator);
        el.clear();
        el.sendKeys(value);
    }
}
