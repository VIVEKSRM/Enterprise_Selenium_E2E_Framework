package core.driver;

import org.openqa.selenium.WebDriver;

import java.util.Objects;

/**
 * Manages the WebDriver instance associated with the current execution thread.
 *
 * <p>This class uses {@link ThreadLocal} so that each parallel TestNG thread
 * receives and uses its own WebDriver instance.</p>
 *
 * <p>For example, if TestNG executes three tests in parallel, each test thread
 * can have a separate browser instance:</p>
 *
 * <pre>
 * Thread 1 --> ChromeDriver instance 1
 * Thread 2 --> ChromeDriver instance 2
 * Thread 3 --> ChromeDriver instance 3
 * </pre>
 *
 * <p>This class follows the utility-class pattern. All methods are static,
 * therefore creating an object of this class is not required.</p>
 */
public final class DriverManager {

    /**
     * Stores one WebDriver instance per execution thread.
     *
     * <p>The ThreadLocal object itself is shared by the framework, but the
     * WebDriver value stored inside it is different for each thread.</p>
     */
    private static final ThreadLocal<WebDriver> DRIVER =
            new ThreadLocal<>();

    /**
     * Private constructor prevents unnecessary object creation.
     */
    private DriverManager() {
    }

    /**
     * Stores a WebDriver instance for the current execution thread.
     *
     * <p>The method validates that the supplied driver is not null and also
     * prevents replacing an already initialized driver for the same thread.</p>
     *
     * @param driver WebDriver instance to store
     * @throws NullPointerException if the supplied driver is null
     * @throws IllegalStateException if a driver already exists for the current thread
     */
    public static void setDriver(WebDriver driver) {

        /*
         * Fail immediately if the caller tries to store a null driver.
         * This prevents null-related failures later in the test execution.
         */
        Objects.requireNonNull(
                driver,
                "WebDriver must not be null"
        );

        /*
         * A thread should not initialize more than one browser instance.
         * The existing driver should first be closed using quitDriver().
         */
        if (DRIVER.get() != null) {
            throw new IllegalStateException(
                    "WebDriver is already initialized for the current thread"
            );
        }

        /*
         * Store the driver against the current execution thread.
         */
        DRIVER.set(driver);
    }

    /**
     * Returns the WebDriver associated with the current execution thread.
     *
     * <p>This method is normally used by BasePage, page objects, and utility
     * classes that need access to the current browser.</p>
     *
     * <p>Example:</p>
     *
     * <pre>
     * WebDriver driver = DriverManager.getDriver();
     * driver.get("https://example.com");
     * </pre>
     *
     * @return WebDriver associated with the current thread
     * @throws IllegalStateException if the driver has not been initialized
     */
    public static WebDriver getDriver() {

        /*
         * Retrieve the driver belonging to the current execution thread.
         */
        WebDriver driver = DRIVER.get();

        /*
         * Do not return null because that would cause a less meaningful
         * NullPointerException in the calling class.
         */
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver is not initialized for the current thread"
            );
        }

        return driver;
    }

    /**
     * Checks whether a WebDriver has already been initialized for the
     * current execution thread.
     *
     * <p>This method is useful in BaseTest setup logic when the framework
     * should create a driver only if one does not already exist.</p>
     *
     * <p>Example:</p>
     *
     * <pre>
     * if (!DriverManager.hasDriver()) {
     *     WebDriver driver = DriverFactory.createDriver();
     *     DriverManager.setDriver(driver);
     * }
     * </pre>
     *
     * @return true if a driver exists for the current thread; otherwise false
     */
    public static boolean hasDriver() {

        /*
         * ThreadLocal.get() returns null when no driver has been stored
         * for the current thread.
         */
        return DRIVER.get() != null;
    }

    /**
     * Closes the WebDriver associated with the current execution thread
     * and removes it from ThreadLocal storage.
     *
     * <p>The ThreadLocal value must be removed after the browser is closed.
     * This is especially important for parallel execution and thread pools,
     * where the same thread may be reused for another test.</p>
     */
    public static void quitDriver() {

        /*
         * Retrieve the driver for the current execution thread.
         */
        WebDriver driver = DRIVER.get();

        try {

            /*
             * A null check makes this method safe to call even when the
             * current thread does not have an initialized driver.
             */
            if (driver != null) {
                driver.quit();
            }

        } finally {

            /*
             * Always remove the driver from ThreadLocal, even if quit()
             * throws an exception.
             *
             * This prevents stale driver references and thread-local memory
             * leaks when TestNG reuses worker threads.
             */
            DRIVER.remove();
        }
    }
}