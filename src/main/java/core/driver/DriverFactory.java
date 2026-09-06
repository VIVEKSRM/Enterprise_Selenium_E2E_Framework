package core.driver;

import core.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class responsible for creating WebDriver instances.
 *
 * <p>This class supports the following execution modes:</p>
 *
 * <ul>
 *     <li>LOCAL - Executes tests on the local machine.</li>
 *     <li>DOCKER - Executes tests on a Selenium Grid running in Docker.</li>
 *     <li>LAMBDATEST - Executes tests on LambdaTest cloud infrastructure.</li>
 * </ul>
 *
 * <p>The browser and execution mode are read from {@link ConfigManager}.
 * This class also configures common browser settings such as headless mode,
 * download preferences, insecure certificate handling, and timeouts.</p>
 *
 * <p>The class uses a private constructor because all methods are static
 * and object creation is not required.</p>
 */
public final class DriverFactory {

    /**
     * Prevents creation of DriverFactory objects.
     */
    private DriverFactory() {
    }

    /**
     * Creates a WebDriver using the framework configuration.
     *
     * <p>This is the main method used by BaseTest and existing test classes.
     * It preserves the original method signature so existing code does not
     * need to be changed.</p>
     *
     * <p>Example:</p>
     *
     * <pre>
     * WebDriver driver = DriverFactory.createDriver();
     * </pre>
     *
     * @return configured WebDriver instance
     * @throws IllegalArgumentException if the browser is unsupported
     * @throws RuntimeException if the driver cannot be created
     */
    public static WebDriver createDriver() {

        /*
         * Read execution configuration from config.properties.
         *
         * Example:
         *
         * execution.mode=LOCAL
         * browser=CHROME
         */
        String mode = ConfigManager.get("execution.mode");
        String browser = ConfigManager.get("browser");

        /*
         * Read optional framework properties.
         *
         * If a property is not configured, a safe default is used.
         */
        boolean headless = isHeadlessEnabled();
        boolean acceptInsecureCertificates = isAcceptInsecureCertificatesEnabled();
        boolean enableDownloads = isDownloadsEnabled();

        String downloadDirectory =
                ConfigManager.get("download.directory");

        /*
         * Default execution mode is LOCAL.
         *
         * This preserves the behavior of your existing DriverFactory.
         */
        if (mode == null || mode.isBlank()) {
            mode = "LOCAL";
        }

        /*
         * Default browser is Chrome.
         *
         * This also preserves your previous behavior.
         */
        if (browser == null || browser.isBlank()) {
            browser = "CHROME";
        }

        /*
         * First try Docker execution when configured.
         * If Docker creation fails, the method falls back to LOCAL execution.
         */
        if ("DOCKER".equalsIgnoreCase(mode)) {
            WebDriver dockerDriver = createDockerDriver(
                    browser,
                    headless,
                    acceptInsecureCertificates
            );

            if (dockerDriver != null) {
                configureDriverTimeouts(dockerDriver);
                return dockerDriver;
            }

            System.out.println(
                    "Docker execution was not available. Falling back to LOCAL execution."
            );
        }

        /*
         * Try LambdaTest execution when configured.
         * If remote creation fails, the method falls back to LOCAL execution.
         */
        if ("LAMBDATEST".equalsIgnoreCase(mode)) {
            WebDriver lambdaTestDriver = createLambdaTestDriver(
                    browser,
                    headless,
                    acceptInsecureCertificates
            );

            if (lambdaTestDriver != null) {
                configureDriverTimeouts(lambdaTestDriver);
                return lambdaTestDriver;
            }

            System.out.println(
                    "LambdaTest execution was not available. Falling back to LOCAL execution."
            );
        }

        /*
         * If execution mode is LOCAL, or remote execution failed,
         * create a browser on the local machine.
         */
        WebDriver localDriver = createLocalDriver(
                browser,
                headless,
                acceptInsecureCertificates,
                enableDownloads,
                downloadDirectory
        );

        configureDriverTimeouts(localDriver);

        return localDriver;
    }

    /**
     * Creates a WebDriver using explicit parameters.
     *
     * <p>This overloaded method is useful when a test or another framework
     * component needs to create a browser without relying on configuration
     * properties.</p>
     *
     * <p>Example:</p>
     *
     * <pre>
     * WebDriver driver = DriverFactory.createDriver(
     *         "chrome",
     *         true,
     *         false,
     *         true,
     *         "C:/automation/downloads"
     * );
     * </pre>
     *
     * @param browser browser name, such as chrome, firefox, or edge
     * @param headless whether the browser should run in headless mode
     * @param acceptInsecureCertificates whether SSL certificate errors should be accepted
     * @param enableDownloads whether automatic download preferences should be configured
     * @param downloadDirectory directory where downloaded files should be stored
     * @return configured local WebDriver instance
     * @throws IllegalArgumentException if browser is null, blank, or unsupported
     */
    public static WebDriver createDriver(
            String browser,
            boolean headless,
            boolean acceptInsecureCertificates,
            boolean enableDownloads,
            String downloadDirectory
    ) {

        validateBrowser(browser);

        WebDriver driver;

        /*
         * Convert the browser name to lowercase so that values such as
         * Chrome, CHROME, and chrome are treated the same way.
         */
        switch (browser.trim().toLowerCase()) {

            case "chrome" -> {
                ChromeOptions options = new ChromeOptions();

                configureCommonChromeOptions(
                        options,
                        headless,
                        acceptInsecureCertificates,
                        enableDownloads,
                        downloadDirectory
                );

                driver = createLocalChromeDriver(options);
            }

            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();

                configureCommonFirefoxOptions(
                        options,
                        headless,
                        acceptInsecureCertificates,
                        enableDownloads,
                        downloadDirectory
                );

                driver = createLocalFirefoxDriver(options);
            }

            case "edge" -> {
                EdgeOptions options = new EdgeOptions();

                configureCommonEdgeOptions(
                        options,
                        headless,
                        acceptInsecureCertificates,
                        enableDownloads,
                        downloadDirectory
                );

                driver = createLocalEdgeDriver(options);
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported browser: " + browser
            );
        }

        configureDriverTimeouts(driver);

        return driver;
    }

    /**
     * Creates a local WebDriver based on the selected browser.
     *
     * @param browser browser name
     * @param headless whether headless mode is enabled
     * @param acceptInsecureCertificates whether SSL errors should be accepted
     * @param enableDownloads whether download preferences should be configured
     * @param downloadDirectory download directory path
     * @return local WebDriver instance
     */
    private static WebDriver createLocalDriver(
            String browser,
            boolean headless,
            boolean acceptInsecureCertificates,
            boolean enableDownloads,
            String downloadDirectory) {

        validateBrowser(browser);

        return switch (browser.trim().toLowerCase()) {

            case "chrome" -> {
                ChromeOptions options = new ChromeOptions();

                configureCommonChromeOptions(
                        options,
                        headless,
                        acceptInsecureCertificates,
                        enableDownloads,
                        downloadDirectory
                );

                yield createLocalChromeDriver(options);
            }

            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();

                configureCommonFirefoxOptions(
                        options,
                        headless,
                        acceptInsecureCertificates,
                        enableDownloads,
                        downloadDirectory
                );

                yield createLocalFirefoxDriver(options);
            }

            case "edge" -> {
                EdgeOptions options = new EdgeOptions();

                configureCommonEdgeOptions(
                        options,
                        headless,
                        acceptInsecureCertificates,
                        enableDownloads,
                        downloadDirectory
                );

                yield createLocalEdgeDriver(options);
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported browser: " + browser
            );
        };
    }

    /**
     * Creates a ChromeDriver locally.
     *
     * <p>WebDriverManager downloads and configures the appropriate ChromeDriver
     * binary before the browser is started.</p>
     *
     * @param options configured Chrome options
     * @return ChromeDriver instance
     */
    private static WebDriver createLocalChromeDriver(
            ChromeOptions options
    ) {

        WebDriverManager.chromedriver().setup();

        return new ChromeDriver(options);
    }

    /**
     * Creates a FirefoxDriver locally.
     *
     * @param options configured Firefox options
     * @return FirefoxDriver instance
     */
    private static WebDriver createLocalFirefoxDriver(
            FirefoxOptions options
    ) {

        WebDriverManager.firefoxdriver().setup();

        return new FirefoxDriver(options);
    }

    /**
     * Creates an EdgeDriver locally.
     *
     * @param options configured Edge options
     * @return EdgeDriver instance
     */
    private static WebDriver createLocalEdgeDriver(
            EdgeOptions options
    ) {

        WebDriverManager.edgedriver().setup();

        return new EdgeDriver(options);
    }

    /**
     * Creates a RemoteWebDriver connected to a Selenium Grid in Docker.
     *
     * <p>If the Docker URL is missing or invalid, this method returns null.
     * The caller then falls back to local execution.</p>
     *
     * @param browser browser requested for Docker execution
     * @param headless whether headless mode is enabled
     * @param acceptInsecureCertificates whether SSL errors should be accepted
     * @return remote WebDriver, or null if Docker execution cannot be created
     */
    private static WebDriver createDockerDriver(
            String browser,
            boolean headless,
            boolean acceptInsecureCertificates
    ) {

        String gridUrl = ConfigManager.get("docker.hub.url");

        if (gridUrl == null || gridUrl.isBlank()) {
            System.out.println(
                    "DOCKER mode requested but docker.hub.url is not configured."
            );

            return null;
        }

        try {

            /*
             * ChromeOptions, FirefoxOptions, and EdgeOptions are capabilities
             * that are sent to the remote Selenium Grid.
             */
            switch (browser.trim().toLowerCase()) {

                case "chrome" -> {
                    ChromeOptions options = new ChromeOptions();

                    configureCommonChromeOptions(
                            options,
                            headless,
                            acceptInsecureCertificates,
                            false,
                            null
                    );

                    return new RemoteWebDriver(
                            new URL(gridUrl),
                            options
                    );
                }

                case "firefox" -> {
                    FirefoxOptions options = new FirefoxOptions();

                    configureCommonFirefoxOptions(
                            options,
                            headless,
                            acceptInsecureCertificates,
                            false,
                            null
                    );

                    return new RemoteWebDriver(
                            new URL(gridUrl),
                            options
                    );
                }

                case "edge" -> {
                    EdgeOptions options = new EdgeOptions();

                    configureCommonEdgeOptions(
                            options,
                            headless,
                            acceptInsecureCertificates,
                            false,
                            null
                    );

                    return new RemoteWebDriver(
                            new URL(gridUrl),
                            options
                    );
                }

                default -> throw new IllegalArgumentException(
                        "Unsupported Docker browser: " + browser
                );
            }

        } catch (MalformedURLException exception) {

            System.err.println(
                    "Invalid Docker Grid URL: " + exception.getMessage()
            );

        } catch (Exception exception) {

            System.err.println(
                    "Docker WebDriver creation failed: "
                            + exception.getMessage()
            );
        }

        return null;
    }

    /**
     * Creates a RemoteWebDriver connected to LambdaTest.
     *
     * <p>The LambdaTest URL should already contain the required authentication
     * information, or authentication should be configured through the URL
     * supplied by the project.</p>
     *
     * @param browser browser requested for LambdaTest execution
     * @param headless whether headless mode is enabled
     * @param acceptInsecureCertificates whether SSL errors should be accepted
     * @return remote WebDriver, or null if LambdaTest execution cannot be created
     */
    private static WebDriver createLambdaTestDriver(
            String browser,
            boolean headless,
            boolean acceptInsecureCertificates
    ) {

        String remoteUrl = ConfigManager.get("lambdatest.url");

        if (remoteUrl == null
                || remoteUrl.isBlank()
                || !remoteUrl.startsWith("http")) {

            System.out.println(
                    "LAMBDATEST requested but lambdatest.url is not configured."
            );

            return null;
        }

        try {

            switch (browser.trim().toLowerCase()) {

                case "chrome" -> {
                    ChromeOptions options = new ChromeOptions();

                    configureCommonChromeOptions(
                            options,
                            headless,
                            acceptInsecureCertificates,
                            false,
                            null
                    );

                    return new RemoteWebDriver(
                            new URL(remoteUrl),
                            options
                    );
                }

                case "firefox" -> {
                    FirefoxOptions options = new FirefoxOptions();

                    configureCommonFirefoxOptions(
                            options,
                            headless,
                            acceptInsecureCertificates,
                            false,
                            null
                    );

                    return new RemoteWebDriver(
                            new URL(remoteUrl),
                            options
                    );
                }

                case "edge" -> {
                    EdgeOptions options = new EdgeOptions();

                    configureCommonEdgeOptions(
                            options,
                            headless,
                            acceptInsecureCertificates,
                            false,
                            null
                    );

                    return new RemoteWebDriver(
                            new URL(remoteUrl),
                            options
                    );
                }

                default -> throw new IllegalArgumentException(
                        "Unsupported LambdaTest browser: " + browser
                );
            }

        } catch (MalformedURLException exception) {

            System.err.println(
                    "Invalid LambdaTest URL: " + exception.getMessage()
            );

        } catch (Exception exception) {

            System.err.println(
                    "LambdaTest WebDriver creation failed: "
                            + exception.getMessage()
            );
        }

        return null;
    }

    /**
     * Configures common Chrome browser options.
     *
     * @param options Chrome options object
     * @param headless whether headless mode is enabled
     * @param acceptInsecureCertificates whether SSL errors should be accepted
     * @param enableDownloads whether download preferences should be configured
     * @param downloadDirectory download directory path
     */
    private static void configureCommonChromeOptions(
            ChromeOptions options,
            boolean headless,
            boolean acceptInsecureCertificates,
            boolean enableDownloads,
            String downloadDirectory
    ) {

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        } else {
            options.addArguments("--start-maximized");
        }

        /*
         * These options reduce interruptions caused by browser notifications
         * and pop-up windows during automation.
         */
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");

        if (acceptInsecureCertificates) {
            options.setAcceptInsecureCerts(true);
        }

        if (enableDownloads) {
            configureChromeDownloads(
                    options,
                    downloadDirectory
            );
        }
    }

    /**
     * Configures Chrome download preferences.
     *
     * @param options Chrome options object
     * @param downloadDirectory directory where files should be downloaded
     */
    private static void configureChromeDownloads(
            ChromeOptions options,
            String downloadDirectory
    ) {

        validateDownloadDirectory(downloadDirectory);

        Map<String, Object> preferences = new HashMap<>();

        /*
         * Prevents Chrome from displaying the Save As dialog.
         */
        preferences.put(
                "download.default_directory",
                downloadDirectory
        );

        preferences.put(
                "download.prompt_for_download",
                false
        );

        /*
         * Allows Chrome to use the configured download directory.
         */
        preferences.put(
                "download.directory_upgrade",
                true
        );

        /*
         * Keeps Chrome Safe Browsing enabled.
         */
        preferences.put(
                "safebrowsing.enabled",
                true
        );

        options.setExperimentalOption(
                "prefs",
                preferences
        );
    }

    /**
     * Configures common Firefox browser options.
     *
     * @param options Firefox options object
     * @param headless whether headless mode is enabled
     * @param acceptInsecureCertificates whether SSL errors should be accepted
     * @param enableDownloads whether download preferences should be configured
     * @param downloadDirectory download directory path
     */
    private static void configureCommonFirefoxOptions(
            FirefoxOptions options,
            boolean headless,
            boolean acceptInsecureCertificates,
            boolean enableDownloads,
            String downloadDirectory
    ) {

        if (headless) {
            options.addArguments("-headless");
            options.addArguments("--width=1920");
            options.addArguments("--height=1080");
        }

        if (acceptInsecureCertificates) {
            options.setAcceptInsecureCerts(true);
        }

        if (enableDownloads) {
            configureFirefoxDownloads(
                    options,
                    downloadDirectory
            );
        }
    }

    /**
     * Configures Firefox download preferences.
     *
     * @param options Firefox options object
     * @param downloadDirectory directory where files should be downloaded
     */
    private static void configureFirefoxDownloads(
            FirefoxOptions options,
            String downloadDirectory
    ) {

        validateDownloadDirectory(downloadDirectory);

        /*
         * folderList=2 means that Firefox should use the custom download
         * directory specified by browser.download.dir.
         */
        options.addPreference(
                "browser.download.folderList",
                2
        );

        options.addPreference(
                "browser.download.dir",
                downloadDirectory
        );

        options.addPreference(
                "browser.download.useDownloadDir",
                true
        );

        /*
         * These MIME types are downloaded directly without displaying the
         * native Open/Save dialog.
         */
        options.addPreference(
                "browser.helperApps.neverAsk.saveToDisk",
                "application/pdf,"
                        + "application/octet-stream,"
                        + "application/zip,"
                        + "text/csv,"
                        + "application/vnd.ms-excel,"
                        + "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        /*
         * Opens PDF files as downloads instead of displaying them in the
         * built-in Firefox PDF viewer.
         */
        options.addPreference(
                "pdfjs.disabled",
                true
        );
    }

    /**
     * Configures common Edge browser options.
     *
     * <p>Microsoft Edge uses Chromium, so its configuration is similar to
     * Chrome's configuration.</p>
     *
     * @param options Edge options object
     * @param headless whether headless mode is enabled
     * @param acceptInsecureCertificates whether SSL errors should be accepted
     * @param enableDownloads whether download preferences should be configured
     * @param downloadDirectory download directory path
     */
    private static void configureCommonEdgeOptions(
            EdgeOptions options,
            boolean headless,
            boolean acceptInsecureCertificates,
            boolean enableDownloads,
            String downloadDirectory
    ) {

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        } else {
            options.addArguments("--start-maximized");
        }

        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");

        if (acceptInsecureCertificates) {
            options.setAcceptInsecureCerts(true);
        }

        if (enableDownloads) {
            configureEdgeDownloads(
                    options,
                    downloadDirectory
            );
        }
    }

    /**
     * Configures Edge download preferences.
     *
     * @param options Edge options object
     * @param downloadDirectory directory where files should be downloaded
     */
    private static void configureEdgeDownloads(
            EdgeOptions options,
            String downloadDirectory
    ) {

        validateDownloadDirectory(downloadDirectory);

        Map<String, Object> preferences = new HashMap<>();

        preferences.put(
                "download.default_directory",
                downloadDirectory
        );

        preferences.put(
                "download.prompt_for_download",
                false
        );

        preferences.put(
                "download.directory_upgrade",
                true
        );

        preferences.put(
                "safebrowsing.enabled",
                true
        );

        options.setExperimentalOption(
                "prefs",
                preferences
        );
    }

    /**
     * Applies common WebDriver timeout policies.
     *
     * <p>The framework deliberately disables implicit waits and uses explicit
     * waits through WaitUtils instead. This avoids mixing implicit and explicit
     * waits, which can cause unpredictable synchronization behavior.</p>
     *
     * @param driver WebDriver instance to configure
     */
    private static void configureDriverTimeouts(
            WebDriver driver
    ) {

        driver.manage()
                .timeouts()
                .implicitlyWait(Duration.ZERO);

        driver.manage()
                .timeouts()
                .pageLoadTimeout(Duration.ofSeconds(60));

        driver.manage()
                .timeouts()
                .scriptTimeout(Duration.ofSeconds(30));
    }

    /**
     * Checks whether headless execution is enabled.
     *
     * <p>The system property takes priority over the configuration file.
     * Example:</p>
     *
     * <pre>
     * mvn test -Dheadless=true
     * </pre>
     *
     * @return true if headless mode is enabled; otherwise false
     */
    private static boolean isHeadlessEnabled() {

        String systemProperty = System.getProperty("headless");

        if (systemProperty != null) {
            return Boolean.parseBoolean(systemProperty);
        }

        String configValue = ConfigManager.get("headless");

        return "true".equalsIgnoreCase(configValue);
    }

    /**
     * Checks whether insecure SSL certificates should be accepted.
     *
     * @return true if enabled in configuration; otherwise false
     */
    private static boolean isAcceptInsecureCertificatesEnabled() {

        String value = ConfigManager.get(
                "accept.insecure.certificates"
        );

        return "true".equalsIgnoreCase(value);
    }

    /**
     * Checks whether automatic download preferences should be enabled.
     *
     * @return true if enabled in configuration; otherwise false
     */
    private static boolean isDownloadsEnabled() {

        String value = ConfigManager.get(
                "downloads.enabled"
        );

        return "true".equalsIgnoreCase(value);
    }

    /**
     * Validates the browser value.
     *
     * @param browser browser name
     * @throws IllegalArgumentException if browser is null or blank
     */
    private static void validateBrowser(String browser) {

        if (browser == null || browser.isBlank()) {
            throw new IllegalArgumentException(
                    "Browser must not be null or blank"
            );
        }
    }

    /**
     * Validates the download directory when downloads are enabled.
     *
     * @param downloadDirectory download directory path
     * @throws IllegalArgumentException if the directory is missing
     */
    private static void validateDownloadDirectory(
            String downloadDirectory
    ) {

        if (downloadDirectory == null || downloadDirectory.isBlank()) {
            throw new IllegalArgumentException(
                    "Download directory must be configured when downloads are enabled"
            );
        }
    }
}