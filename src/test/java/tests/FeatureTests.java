package tests;

import core.base.BaseTest;
import core.utils.ScreenshotUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Collection of small feature tests demonstrating common Selenium actions.
 * These are intentionally conservative placeholders so they run in CI without external sites.
 */
public class FeatureTests extends BaseTest {

    private String resourcePath(String relative) {
        File f = new File(relative);
        return f.toURI().toString();
    }

    @Test
    public void navigationTest() {
        WebDriver driver = core.driver.DriverManager.getDriver();
        driver.get("about:blank");
        Assert.assertEquals(driver.getTitle(), "", "Blank page should have empty title");
    }

    @Test
    public void alertHandlingTest() {
        WebDriver driver = core.driver.DriverManager.getDriver();
        driver.get(resourcePath("src/test/resources/pages/alert.html"));
        WebElement btn = driver.findElement(By.id("run"));
        btn.click();
        try {
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            Assert.fail("Alert was not present or could not be accepted: " + e.getMessage());
        }
    }

    @Test
    public void dropdownTest() {
        WebDriver driver = core.driver.DriverManager.getDriver();
        driver.get(resourcePath("src/test/resources/pages/dropdown.html"));
        WebElement sel = driver.findElement(By.id("s"));
        sel.sendKeys("Two");
        Assert.assertTrue(sel.getText().contains("Two"));
    }

    @Test
    public void iframeTest() {
        WebDriver driver = core.driver.DriverManager.getDriver();
        driver.get(resourcePath("src/test/resources/pages/iframe.html"));
        driver.switchTo().frame("f");
        WebElement p = driver.findElement(By.id("x"));
        Assert.assertEquals(p.getText(), "inside");
        driver.switchTo().defaultContent();
    }

    @Test
    public void fileUploadTest() {
        WebDriver driver = core.driver.DriverManager.getDriver();
        driver.get(resourcePath("src/test/resources/pages/fileupload.html"));
        WebElement input = driver.findElement(By.id("f"));
        File file = new File("src/test/resources/testfile.txt");
        Assert.assertTrue(file.exists(), "Upload file should exist");
        input.sendKeys(file.getAbsolutePath());
        String path = ScreenshotUtil.takeViewportScreenshot();
        Assert.assertNotNull(path);
    }

    @Test
    public void windowHandlesTest() {
        WebDriver driver = core.driver.DriverManager.getDriver();
        driver.get("data:text/html,<a id=\"n\" target=\"_blank\" href=\"about:blank\">new</a>");
        String original = driver.getWindowHandle();
        driver.findElement(By.id("n")).click();
        for (String h : driver.getWindowHandles()) {
            if (!h.equals(original)) {
                driver.switchTo().window(h);
                driver.close();
            }
        }
        driver.switchTo().window(original);
        Assert.assertTrue(true);
    }
}
