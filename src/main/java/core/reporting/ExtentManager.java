package core.reporting;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

/**
 * Central Extent Report Manager
 */
public final class ExtentManager {

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    private ExtentManager() {}

    public static void init() {
        ExtentSparkReporter reporter =
                new ExtentSparkReporter("reports/ExtentReport.html");
        reporter.config().setReportName("Enterprise Selenium E2E Report");
        reporter.config().setDocumentTitle("Automation Execution Report");

        extent = new ExtentReports();
        extent.attachReporter(reporter);
    }

    public static void createTest(String testName) {
        test.set(extent.createTest(testName));
    }

    public static void pass(String message) {
        test.get().pass(message);
    }

    public static void fail(String message) {
        test.get().fail(message);
    }

    public static void fail(String message, String screenshotPath) {
        try {
            test.get().fail(message,
                MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
        } catch (Exception e) {
            test.get().fail("Screenshot attach failed: " + e.getMessage());
        }
    }

    public static void flush() {
        extent.flush();
    }
}
