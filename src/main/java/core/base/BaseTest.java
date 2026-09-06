package core.base;

import core.config.ConfigManager;
import core.driver.DriverFactory;
import core.driver.DriverManager;
import core.reporting.ExtentManager;
import core.video.VideoRecorderUtil;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public abstract class BaseTest {

    private final ThreadLocal<Boolean> driverCreated =
            ThreadLocal.withInitial(() -> false);

    private final ThreadLocal<Boolean> extentCreated =
            ThreadLocal.withInitial(() -> false);

    @BeforeMethod(alwaysRun = true)
    public void setup(Method method) {

        // Create an Extent test if the listener has not already created one
        try {
            if (!ExtentManager.hasTest()) {
                ExtentManager.startTest(method.getName());
                extentCreated.set(true);
            }
        } catch (Exception exception) {
            System.err.println(
                    "Unable to initialize Extent test: "
                            + exception.getMessage()
            );
        }

        // Create WebDriver only if it does not already exist
        if (!DriverManager.hasDriver()) {

            WebDriver driver = DriverFactory.createDriver();

            DriverManager.setDriver(driver);
            driverCreated.set(true);

            /*
             * Open the application URL before the test starts.
             * The URL is maintained in config.properties.
             */
            String applicationUrl = ConfigManager.get("application.url");

            if (applicationUrl == null || applicationUrl.isBlank()) {
                throw new IllegalStateException(
                        "application.url is missing from config.properties"
                );
            }

            DriverManager.getDriver().get(applicationUrl);

            // Start video recording if configured
            try {
                VideoRecorderUtil.start(method.getName());
            } catch (Exception exception) {
                System.err.println(
                        "Unable to start video recording: "
                                + exception.getMessage()
                );
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    public void teardown(ITestResult result) {

        // Stop video and quit the driver only if this class created it
        if (Boolean.TRUE.equals(driverCreated.get())) {

            try {
                VideoRecorderUtil.stop();
            } catch (Exception exception) {
                System.err.println(
                        "Unable to stop video recording: "
                                + exception.getMessage()
                );
            } finally {
                DriverManager.quitDriver();
                driverCreated.remove();
            }
        }

        // Flush Extent only if this class created the test
        if (Boolean.TRUE.equals(extentCreated.get())) {

            try {
                ExtentManager.flush();
            } catch (Exception exception) {
                System.err.println(
                        "Unable to flush Extent report: "
                                + exception.getMessage()
                );
            } finally {
                extentCreated.remove();
            }
        }
    }
}