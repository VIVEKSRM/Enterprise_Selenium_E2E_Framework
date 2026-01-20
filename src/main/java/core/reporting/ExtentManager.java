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

    public static synchronized void init() {
        if (extent != null) return;
        ExtentSparkReporter reporter =
                new ExtentSparkReporter("reports/ExtentReport.html");
        reporter.config().setReportName("Enterprise Selenium E2E Report");
        reporter.config().setDocumentTitle("Automation Execution Report");

        extent = new ExtentReports();
        extent.attachReporter(reporter);
    }

    public static boolean hasTest() {
        return test.get() != null;
    }

    public static void startTest(String testName) {
        if (extent == null) init();
        createTest(testName);
    }

    public static void createTest(String testName) {
        if (extent == null) init();
        test.set(extent.createTest(testName));
    }

    public static void log(String message) {
        try {
            if (test.get() != null) {
                test.get().info(message);
            }
        } catch (Exception ignored) {
            // best-effort: do not throw to avoid changing test outcomes
        }
    }

    public static void pass(String message) {
        try {
            if (test.get() != null) test.get().pass(message);
        } catch (Exception ignored) {}
    }

    public static void fail(String message) {
        try {
            if (test.get() != null) test.get().fail(message);
        } catch (Exception ignored) {}
    }

    public static void fail(String message, String screenshotPath) {
        try {
            if (test.get() != null) {
                try {
                    test.get().fail(message,
                        MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
                } catch (Exception e) {
                    test.get().fail(message + " (screenshot attach failed: " + e.getMessage() + ")");
                }
            }
        } catch (Exception ignored) {}
    }

    public static void attachScreenshot(String screenshotPath, String title) {
        try {
            if (test.get() != null) {
                try {
                    test.get().info(title,
                        MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
                } catch (Exception e) {
                    test.get().info(title + " (screenshot attach failed: " + e.getMessage() + ")");
                }
            }
        } catch (Exception ignored) {}
    }

    public static void flush() {
        try {
            if (extent != null) extent.flush();
        } catch (Exception ignored) {}
    }
}
