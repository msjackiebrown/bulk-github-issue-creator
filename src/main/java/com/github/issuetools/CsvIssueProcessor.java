package com.github.issuetools;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Processes issues from a CSV file.
 */
public class CsvIssueProcessor implements IssueProcessor {

    @Override
    public IssueCreationResult processIssues(String token, String repository, String filePath, boolean dryRun) throws Exception {
        // Parse the CSV file
        List<Issue> issues = new ArrayList<>();
        
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            
            for (CSVRecord record : csvParser) {
                Issue issue = new Issue();
                issue.setTitle(record.get("title"));
                issue.setBody(record.get("body"));
                
                if (record.isMapped("labels")) {
                    issue.setLabels(record.get("labels"));
                }
                
                if (record.isMapped("assignees")) {
                    issue.setAssignees(record.get("assignees"));
                }
                
                issues.add(issue);
            }
        }
        
        System.out.println("Found " + issues.size() + " issues in the CSV file.");
        
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
