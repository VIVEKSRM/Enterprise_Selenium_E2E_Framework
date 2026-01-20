package core.base;

import core.driver.DriverFactory;
import core.driver.DriverManager;
import core.video.VideoRecorderUtil;
import core.reporting.ExtentManager;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

/**
 * Optional BaseTest that initializes/quits WebDriver if not already initialized by a listener.
 * Use this if you prefer per-test setup in test classes instead of using the TestListener.
 */
public class BaseTest {

    private static ThreadLocal<Boolean> created = ThreadLocal.withInitial(() -> false);
    private static ThreadLocal<Boolean> extentCreated = ThreadLocal.withInitial(() -> false);

    @BeforeMethod
    public void setup(Method method) {
        // Ensure an Extent test exists for IDE/manual runs when listener isn't present
        try {
            if (!ExtentManager.hasTest()) {
                ExtentManager.startTest(method.getName());
                extentCreated.set(true);
            }
        } catch (Exception ignored) {}

        if (DriverManager.getDriver() == null) {
            WebDriver wd = DriverFactory.createDriver();
            DriverManager.setDriver(wd);
            created.set(true);

            // Start video for this test (best-effort)
            try {
                VideoRecorderUtil.start(method.getName());
            } catch (Exception ignored) {}
        }
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        if (Boolean.TRUE.equals(created.get())) {
            try { VideoRecorderUtil.stop(); } catch (Exception ignored) {}
            DriverManager.quitDriver();
            created.remove();
        }

        // Flush Extent report only if BaseTest created it (listener will flush itself)
        try {
            if (Boolean.TRUE.equals(extentCreated.get())) {
                ExtentManager.flush();
                extentCreated.remove();
            }
        } catch (Exception ignored) {}
    }
}
