package core.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtils {

    private final WebDriverWait wait;

    public WaitUtils(WebDriver driver) {
        this.wait = new WebDriverWait(driver,Duration.ofSeconds(15)
        );
    }

    public WebElement visible(By locator) {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(locator)
        );
    }

    public WebElement clickable(By locator) {
        return wait.until(
                ExpectedConditions.elementToBeClickable(locator)
        );
    }

    public WebElement present(By locator) {
        return wait.until(
                ExpectedConditions.presenceOfElementLocated(locator)
        );
    }

    public boolean displayed(By locator) {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(locator)
        ).isDisplayed();
    }

    public boolean exists(By locator) {
        return wait.until(
                ExpectedConditions.presenceOfElementLocated(locator)
        ) != null;
    }

    public void waitForUrl(String url) {
        wait.until(ExpectedConditions.urlToBe(url));
    }

    public void waitForTitle(String title) {
        wait.until(ExpectedConditions.titleIs(title));
    }

    public void waitForText(By locator, String text) {
        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        locator,
                        text
                )
        );
    }

    public void waitForInvisibility(By locator) {
        wait.until(
                ExpectedConditions.invisibilityOfElementLocated(locator)
        );
    }

    public void waitForPageLoad() {
        wait.until(driver ->
                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript(
                                "return document.readyState"
                        )
                        .equals("complete")
        );
    }
}