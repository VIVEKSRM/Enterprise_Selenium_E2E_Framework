package core.utils;

import core.driver.DriverManager;
import org.openqa.selenium.*;
import java.io.File;
import java.nio.file.Files;

/**
 * Screenshot utilities
 */
public final class ScreenshotUtil {

    private ScreenshotUtil() {}

    public static String takeScreenshot(String name) {
        try {
            File src = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.FILE);

            String path = "reports/screenshots/" + name + "_" + System.currentTimeMillis() + ".png";
            File dest = new File(path);
            dest.getParentFile().mkdirs();

            Files.copy(src.toPath(), dest.toPath());
            return path;

        } catch (Exception e) {
            return null;
        }
    }

    public static String takeViewportScreenshot() {
        return takeScreenshot("viewport");
    }

    public static String takeFullPageScreenshot() {
        return takeScreenshot("fullpage");
    }
}
