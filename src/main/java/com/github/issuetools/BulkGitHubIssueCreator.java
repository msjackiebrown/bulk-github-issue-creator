package com.github.issuetools;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * Main application class for the Bulk GitHub Issue Creator.
 * This tool creates GitHub issues in bulk from JSON or CSV files.
 */
@Command(name = "bulk-github-issue-creator", 
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Creates GitHub issues in bulk from JSON or CSV files")
public class BulkGitHubIssueCreator implements Callable<Integer> {

    @Option(names = {"-t", "--token"}, 
            description = "GitHub personal access token. If not provided, will look for GITHUB_TOKEN environment variable.",
            defaultValue = "${env:GITHUB_TOKEN}")
    private String token;

    @Option(names = {"-r", "--repo"}, 
            description = "GitHub repository in the format 'owner/repo'",
            required = true)
    private String repository;

    @Option(names = {"-f", "--file"}, 
            description = "Path to the JSON or CSV file containing issue data",
            required = true)
    private String issuesFile;

    @Option(names = {"-d", "--dry-run"}, 
            description = "Perform a dry run without creating actual issues")
    private boolean dryRun = false;

    @Option(names = {"--format"}, 
            description = "Force file format (json or csv). If not specified, it will be inferred from the file extension.")
    private String format;

    public static void main(String[] args) {
        // Do not call System.exit here to allow embedding in web UI
        new CommandLine(new BulkGitHubIssueCreator()).execute(args);
    }

    @Override
    public Integer call() throws Exception {
        try {
            System.out.println("Bulk GitHub Issue Creator");
            System.out.println("------------------------");
            System.out.println("Repository: " + repository);
            System.out.println("Issues file: " + issuesFile);
            
            // Determine the file format
            FileFormat fileFormat = determineFileFormat();
            System.out.println("File format: " + fileFormat);
            
            // Create the appropriate processor
            IssueProcessor processor = createProcessor(fileFormat);
            
            // Process the issues
            IssueCreationResult result = processor.processIssues(token, repository, issuesFile, dryRun);
            
            // Print the results
            System.out.println("\nResults:");
            System.out.println("  Successfully created: " + result.getSuccessCount());
            System.out.println("  Failed: " + result.getFailureCount());
            
            if (result.getFailureCount() > 0) {
                System.out.println("\nFailed issues:");
                result.getFailures().forEach((title, error) -> 
                    System.out.println("  - " + title + ": " + error));
                return 1;
            }
            
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
    
    private FileFormat determineFileFormat() {
        if (format != null) {
            if ("json".equalsIgnoreCase(format)) {
                return FileFormat.JSON;
            } else if ("csv".equalsIgnoreCase(format)) {
                return FileFormat.CSV;
            } else {
                throw new IllegalArgumentException("Unsupported format: " + format + ". Use 'json' or 'csv'.");
            }
        }
        
        if (issuesFile.toLowerCase().endsWith(".json")) {
            return FileFormat.JSON;
        } else if (issuesFile.toLowerCase().endsWith(".csv")) {
            return FileFormat.CSV;
        } else {
            throw new IllegalArgumentException("Cannot determine file format from extension. " +
                    "Please specify the format using --format option.");
        }
    }
    
    private IssueProcessor createProcessor(FileFormat format) {
        return switch (format) {
            case JSON -> new JsonIssueProcessor();
            case CSV -> new CsvIssueProcessor();
        };
    }
}
