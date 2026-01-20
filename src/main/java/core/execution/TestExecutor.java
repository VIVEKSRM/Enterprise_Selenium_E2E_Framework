package core.execution;

import core.db.SqlLogger;
import core.driver.DriverManager;
import core.reporting.ExtentManager;
import core.utils.ScreenshotUtil;
import org.testng.ITestResult;

/**
 * Execute-Around Pattern implementation
 */
public class TestExecutor {

    public static void startTest(String testName) {
        // Driver init already handled
        System.out.println("Test Started: " + testName);
        try {
            // Create an extent test only if one hasn't been created by TestListener
            if (!ExtentManager.hasTest()) {
                ExtentManager.startTest(testName);
                ExtentManager.log("Test Started: " + testName);
            }
        } catch (Exception ignored) {}
    }

    public static void endTest(ITestResult result) {

        String status = result.isSuccess() ? "PASS" : "FAIL";
        String logs = result.isSuccess() ? "Execution successful"
                : result.getThrowable().getMessage();

        SqlLogger.logExecution(
                result.getMethod().getMethodName(),
                result.getMethod().getMethodName(),
                System.getProperty("user.name"),
                status,
                logs
        );

        try {
            if (!ExtentManager.hasTest()) {
                // If we created the test, log the result and flush
                if (result.isSuccess()) {
                    ExtentManager.pass("Test Passed");
                } else {
                    String path = ScreenshotUtil.takeViewportScreenshot();
                    ExtentManager.fail(result.getThrowable().getMessage(), path);
                    ExtentManager.attachScreenshot(path, "Failure Screenshot");
                }
                ExtentManager.flush();
            }
        } catch (Exception ignored) {}

        DriverManager.quitDriver();
    }
}
