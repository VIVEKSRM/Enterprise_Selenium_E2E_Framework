package core.db;

/**
 * Simple SQL logger placeholder
 */
public final class SqlLogger {

    private SqlLogger() {}

    public static void logExecution(String testName, String scenario, String user, String status, String logs) {
        System.out.println("SQL LOG -> " + testName + " | " + status + " | " + logs);
    }
}
