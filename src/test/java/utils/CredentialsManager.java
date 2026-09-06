package utils;

import core.config.ConfigManager;

/**
 * Provides commonly used test credentials.
 *
 * <p>Credential values are resolved through ConfigManager, which first
 * checks JVM system properties and then config.properties.</p>
 */
public final class CredentialsManager {

    private CredentialsManager() {
    }

    public static String getValidEmail() {
        return getRequired("login.valid.email");
    }

    public static String getValidPassword() {
        return getRequired("login.valid.password");
    }

    public static String getInvalidPassword() {
        return getRequired("login.invalid.password");
    }

    private static String getRequired(String key) {
        String value = ConfigManager.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required credential configuration is missing: " + key
            );
        }

        return value;
    }
}
