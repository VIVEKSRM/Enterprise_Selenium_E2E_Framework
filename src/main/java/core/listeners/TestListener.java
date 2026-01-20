package core.listeners;

import core.reporting.ExtentManager;
import core.utils.ScreenshotUtil;
import core.driver.DriverManager;
import core.driver.DriverFactory;
import core.video.VideoRecorderUtil;
import org.testng.*;

/**
 * TestNG listener that initializes reporting and optionally creates/quits drivers
 * If a driver already exists (e.g. created by BaseTest), the listener will not replace it.
 */
public class TestListener implements ITestListener {

    private static ThreadLocal<Boolean> listenerCreated = ThreadLocal.withInitial(() -> false);

    @Override
    public void onStart(ITestContext context) {
        ExtentManager.init();
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentManager.createTest(result.getMethod().getMethodName());

        // Initialize WebDriver only if not already set by BaseTest or other setup
        try {
            if (DriverManager.getDriver() == null) {
                org.openqa.selenium.WebDriver webDriver = DriverFactory.createDriver();
                DriverManager.setDriver(webDriver);
                listenerCreated.set(true);

                // Start video recording (best-effort) only when listener created the driver
                try {
                    VideoRecorderUtil.start(result.getMethod().getMethodName());
                } catch (Exception ignored) {}
            } else {
                listenerCreated.set(false);
            }
        } catch (Exception e) {
            System.err.println("Driver initialization failed in listener: " + e.getMessage());
            // don't throw here to allow tests that don't need driver to run; but log
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentManager.pass("Test Passed");
        cleanupIfListenerCreated();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String path = ScreenshotUtil.takeViewportScreenshot();
        ExtentManager.fail(result.getThrowable().getMessage(), path);
        cleanupIfListenerCreated();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        cleanupIfListenerCreated();
    }

    private void cleanupIfListenerCreated() {
        try {
            if (Boolean.TRUE.equals(listenerCreated.get())) {
                try { VideoRecorderUtil.stop(); } catch (Exception ignored) {}
                DriverManager.quitDriver();
                listenerCreated.remove();
            }
        } catch (Exception e) {
            System.err.println("Error during listener cleanup: " + e.getMessage());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
    }
}
