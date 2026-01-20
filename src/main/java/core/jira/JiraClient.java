package core.jira;

import core.config.ConfigManager;

/**
 * Jira integration client (step update + attachment)
 */
public class JiraClient {

    public static void updateStep(int stepNumber, String status, String screenshotPath) {

        if (!ConfigManager.isOn("jira.enabled")) {
            return;
        }

        // Placeholder for REST API integration
        System.out.println(
            "Jira Step Update -> Step: " + stepNumber +
            ", Status: " + status +
            ", Screenshot: " + screenshotPath
        );
    }
}
