package core.driver;

import core.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;

/**
 * Driver factory to create WebDriver instances for LOCAL, DOCKER (Selenium Grid), and remote (LAMBDATEST) execution.
 */
public final class DriverFactory {

    private DriverFactory() {}

    public static WebDriver createDriver() {
        String mode = ConfigManager.get("execution.mode");
        String browser = ConfigManager.get("browser");

        if (mode == null) {
            mode = "LOCAL";
        }

        // Docker Selenium Grid execution
        if ("DOCKER".equalsIgnoreCase(mode)) {
            String gridUrl = ConfigManager.get("docker.hub.url");
            if (gridUrl != null && gridUrl.startsWith("http")) {
                try {
                    if (browser == null || "CHROME".equalsIgnoreCase(browser)) {
                        ChromeOptions opts = new ChromeOptions();
                        opts.addArguments("--disable-gpu");
                        opts.addArguments("--no-sandbox");
                        opts.addArguments("--disable-dev-shm-usage");
                        return new RemoteWebDriver(new URL(gridUrl), opts);
                    } else if ("FIREFOX".equalsIgnoreCase(browser)) {
                        FirefoxOptions opts = new FirefoxOptions();
                        return new RemoteWebDriver(new URL(gridUrl), opts);
                    }
                } catch (Exception e) {
                    System.err.println("Docker WebDriver creation failed, falling back to LOCAL: " + e.getMessage());
                }
            } else {
                System.out.println("DOCKER mode requested but docker.hub.url is not configured; falling back to LOCAL");
            }
        }

        // LambdaTest Cloud execution
        if ("LAMBDATEST".equalsIgnoreCase(mode)) {
            String remoteUrl = ConfigManager.get("lambdatest.url");
            if (remoteUrl != null && remoteUrl.startsWith("http") && !remoteUrl.contains("username") && !remoteUrl.contains("accesskey")) {
                try {
                    ChromeOptions opts = new ChromeOptions();
                    opts.addArguments("--disable-gpu");
                    return new RemoteWebDriver(new URL(remoteUrl), opts);
                } catch (Exception e) {
                    System.err.println("Remote WebDriver creation failed, falling back to LOCAL: " + e.getMessage());
                }
            } else {
                System.out.println("LAMBDATEST requested but lambdatest.url is not configured with credentials; falling back to LOCAL");
            }
        }

        // Default: LOCAL execution
        if (browser == null || "CHROME".equalsIgnoreCase(browser)) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            // run headless by default in CI if property set
            if ("true".equalsIgnoreCase(System.getProperty("headless"))) {
                options.addArguments("--headless=new");
            }
            return new ChromeDriver(options);
        } else if ("FIREFOX".equalsIgnoreCase(browser)) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
            if ("true".equalsIgnoreCase(System.getProperty("headless"))) {
                options.addArguments("--headless");
            }
            return new FirefoxDriver(options);
        }

        throw new RuntimeException("Unsupported browser: " + browser);
    }
}
