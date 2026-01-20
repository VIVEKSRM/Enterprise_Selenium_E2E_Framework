package core.db;

import core.reporting.ExtentManager;

/**
 * Simple SQL logger placeholder
 */
public final class SqlLogger {

    private SqlLogger() {}

    public static void logExecution(String testName, String scenario, String user, String status, String logs) {
        System.out.println("SQL LOG -> " + testName + " | " + status + " | " + logs);
        try {
            // Best-effort: also write to Extent report without changing test outcome
            ExtentManager.log("SQL LOG -> " + testName + " | " + status + " | " + logs);
        } catch (Exception ignored) {}
    }
}
