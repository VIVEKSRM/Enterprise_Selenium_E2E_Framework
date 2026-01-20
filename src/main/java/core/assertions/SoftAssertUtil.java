package core.assertions;

import core.reporting.ExtentManager;

/**
 * Simplified soft-assert utility that prints messages and throws on failure in this demo.
 */
public final class SoftAssertUtil {

    private SoftAssertUtil() {}

    public static void verify(boolean condition, String passMsg, String failMsg) {
        if (condition) {
            System.out.println("ASSERT PASS: " + passMsg);
            try { ExtentManager.log("ASSERT PASS: " + passMsg); } catch (Exception ignored) {}
        } else {
            try { ExtentManager.fail(failMsg); } catch (Exception ignored) {}
            throw new AssertionError(failMsg);
        }
    }

    public static void verify(boolean condition, String passMsg, String failMsg, int retry) {
        // Simple placeholder: retries are not actually implemented in this demo.
        verify(condition, passMsg, failMsg);
    }

    // Support legacy/extended signature used in tests: (condition, passMsg, failMsg, <nullable>, retry)
    public static void verify(boolean condition, String passMsg, String failMsg, Object ignored, int retry) {
        verify(condition, passMsg, failMsg, retry);
    }
}
