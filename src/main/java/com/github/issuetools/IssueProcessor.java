package com.github.issuetools;

/**
 * Interface defining the contract for issue processors.
 */
public interface IssueProcessor {
    /**
     * Process issues from a file and create them on GitHub.
     *
     * @param token      GitHub personal access token
     * @param repository GitHub repository in the format 'owner/repo'
     * @param filePath   Path to the file containing issue data
     * @param dryRun     If true, performs a dry run without creating actual issues
     * @return Result of the operation
     * @throws Exception If an error occurs during processing
     */
    IssueCreationResult processIssues(String token, String repository, String filePath, boolean dryRun) throws Exception;
}
