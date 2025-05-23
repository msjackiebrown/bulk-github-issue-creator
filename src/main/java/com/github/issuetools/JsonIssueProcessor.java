package com.github.issuetools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Processes issues from a JSON file.
 */
public class JsonIssueProcessor implements IssueProcessor {

    @Override
    public IssueCreationResult processIssues(String token, String repository, String filePath, boolean dryRun) throws Exception {
        // Parse the JSON file
        ObjectMapper mapper = new ObjectMapper();
        List<Issue> issues = mapper.readValue(new File(filePath), new TypeReference<List<Issue>>() {});
        
        System.out.println("Found " + issues.size() + " issues in the JSON file.");
        
        int successCount = 0;
        int failureCount = 0;
        Map<String, String> failures = new HashMap<>();
        
        // If dry run, just print the issues
        if (dryRun) {
            System.out.println("\nDRY RUN - The following issues would be created:");
            issues.forEach(issue -> System.out.println("  - " + issue.getTitle()));
            return new IssueCreationResult(issues.size(), 0, failures);
        }
        
        // Connect to GitHub
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        String[] repoParts = repository.split("/");
        
        if (repoParts.length != 2) {
            throw new IllegalArgumentException("Repository must be in the format 'owner/repo'");
        }
        
        String owner = repoParts[0];
        String repo = repoParts[1];
        
        // Create the issues
        for (Issue issue : issues) {
            try {
                String title = issue.getTitle();
                if (title == null || title.isEmpty()) {
                    failureCount++;
                    failures.put("[Missing Title]", "Issue title is required");
                    continue;
                }
                
                System.out.println("Creating issue: " + title);
                
                GHIssueBuilder issueBuilder = github.getRepository(repository)
                        .createIssue(title)
                        .body(issue.getBody());                // Add labels if specified
                if (issue.getLabels() != null && !issue.getLabels().isEmpty()) {
                    String[] labels = Arrays.stream(issue.getLabels().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toArray(String[]::new);
                    
                    if (labels.length > 0) {
                        for (String label : labels) {
                            issueBuilder.label(label);
                        }
                    }
                }
                
                // Add assignees if specified
                if (issue.getAssignees() != null && !issue.getAssignees().isEmpty()) {
                    List<String> assigneeList = Arrays.stream(issue.getAssignees().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                    
                    if (!assigneeList.isEmpty()) {
                        for (String assignee : assigneeList) {
                            issueBuilder.assignee(assignee);
                        }
                    }
                }
                
                // Create the issue
                issueBuilder.create();
                successCount++;
            } catch (Exception e) {
                failureCount++;
                failures.put(issue.getTitle(), e.getMessage());
            }
        }
        
        return new IssueCreationResult(successCount, failureCount, failures);
    }
}
