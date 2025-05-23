package com.github.issuetools;

/**
 * Represents a GitHub issue to be created.
 */
public class Issue {
    private String title;
    private String body;
    private String labels;
    private String assignees;

    public Issue() {
    }

    public Issue(String title, String body, String labels, String assignees) {
        this.title = title;
        this.body = body;
        this.labels = labels;
        this.assignees = assignees;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getAssignees() {
        return assignees;
    }

    public void setAssignees(String assignees) {
        this.assignees = assignees;
    }
}
