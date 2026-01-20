package core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Map;

/**
 * Simple credentials loader for tests (expects credentials.json in resources)
 */
public final class CredentialUtil {

    private CredentialUtil() {}

    public static Map<String, String> load() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File("src/test/resources/credentials.json"), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load credentials", e);
        }
    }
}
