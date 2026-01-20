package core.execution;

import core.db.SqlLogger;
import core.driver.DriverManager;
import org.testng.ITestResult;

/**
 * Execute-Around Pattern implementation
 */
public class TestExecutor {

    public static void startTest(String testName) {
        // Driver init already handled
        System.out.println("Test Started: " + testName);
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

        DriverManager.quitDriver();
    }
}
