package com.github.issuetools;

import java.util.Map;

/**
 * Class to store the results of the issue creation process.
 */
public class IssueCreationResult {
    private int successCount;
    private int failureCount;
    private Map<String, String> failures; // title -> error message

    public IssueCreationResult(int successCount, int failureCount, Map<String, String> failures) {
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.failures = failures;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public Map<String, String> getFailures() {
        return failures;
    }
}
