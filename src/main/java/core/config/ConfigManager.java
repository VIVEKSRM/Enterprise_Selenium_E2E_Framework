package core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration manager for the automation framework.
 *
 * <p>Configuration values are loaded from config.properties located
 * under src/test/resources.</p>
 *
 * <p>System properties supplied through Maven or CI/CD take priority
 * over values defined in the properties file.</p>
 */
public final class ConfigManager {

    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    /**
     * Prevents object creation.
     */
    private ConfigManager() {
    }

    /**
     * Loads config.properties from the classpath.
     *
     * @throws IllegalStateException if the configuration file is missing
     *                               or cannot be loaded
     */
    private static void loadProperties() {

        try (InputStream inputStream =
                     ConfigManager.class
                             .getClassLoader()
                             .getResourceAsStream("config.properties")) {

            if (inputStream == null) {
                throw new IllegalStateException(
                        "config.properties was not found under src/test/resources"
                );
            }

            PROPERTIES.load(inputStream);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to load config.properties",
                    exception
            );
        }
    }

    /**
     * Returns the configuration value for the supplied key.
     *
     * <p>A JVM system property takes priority over the value in
     * config.properties.</p>
     *
     * @param key configuration key
     * @return configured value, or null if the key is unavailable
     */
    public static String get(String key) {

        String systemValue = System.getProperty(key);

        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        return PROPERTIES.getProperty(key);
    }

    /**
     * Checks whether a configuration value is set to ON.
     *
     * @param key configuration key
     * @return true when the value is ON, otherwise false
     */
    public static boolean isOn(String key) {
        return "ON".equalsIgnoreCase(get(key));
    }
}