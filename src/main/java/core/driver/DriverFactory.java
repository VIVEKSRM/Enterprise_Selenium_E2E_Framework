package core.driver;

import core.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;

/**
 * Simple Driver factory to create WebDriver instances for LOCAL and remote (LAMBDATEST) execution.
 */
public final class DriverFactory {

    private DriverFactory() {}

    public static WebDriver createDriver() {
        String mode = ConfigManager.get("execution.mode");
        String browser = ConfigManager.get("browser");

        if (mode == null) {
            mode = "LOCAL";
        }

        // Use remote only when explicitly configured with a usable URL (avoid placeholder credentials)
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

        // Default: LOCAL
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
        }

        // Could add other browsers here
        throw new RuntimeException("Unsupported browser: " + browser);
    }
}
