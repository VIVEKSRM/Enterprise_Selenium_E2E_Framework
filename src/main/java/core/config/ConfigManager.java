package core.config;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Central configuration manager
 */
public final class ConfigManager {

    private static Properties props = new Properties();

    static {
        try {
            props.load(new FileInputStream("src/test/resources/config.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static String get(String key) {
        return System.getProperty(key, props.getProperty(key));
    }

    public static boolean isOn(String key) {
        return "ON".equalsIgnoreCase(get(key));
    }
}
