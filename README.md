# Bulk GitHub Issue Creator

A command-line tool to create GitHub issues in bulk from JSON or CSV files.

## Features

- Create multiple GitHub issues at once
- Support for both JSON and CSV input formats
- Specify labels and assignees for each issue
- Dry-run mode to preview issues before creation
- Detailed reporting of successful/failed creations

## Prerequisites

- Java 21 or higher
- Maven (for building from source)
- GitHub Personal Access Token with appropriate permissions

## Installation

### Download pre-built JAR

Download the latest release from the [Releases](https://github.com/yourusername/bulk-github-issue-creator/releases) page.

### Build from source

```bash
git clone https://github.com/yourusername/bulk-github-issue-creator.git
cd bulk-github-issue-creator
mvn clean package
```

This will create a runnable JAR file in the `target` directory.

## Usage

### Basic Usage

```bash
java -jar bulk-github-issue-creator.jar --repo owner/repository --file issues.json
```

You will be prompted for your GitHub token, or you can set the `GITHUB_TOKEN` environment variable.

### Command-line Options

```
Usage: bulk-github-issue-creator [-dh] [--format=<format>] -f=<issuesFile>
                                 -r=<repository> [-t=<token>] [--version]
Creates GitHub issues in bulk from JSON or CSV files
  -d, --dry-run              Perform a dry run without creating actual issues
  -f, --file=<issuesFile>    Path to the JSON or CSV file containing issue data
      --format=<format>      Force file format (json or csv). If not specified,
                               it will be inferred from the file extension.
  -h, --help                 Show this help message and exit.
  -r, --repo=<repository>    GitHub repository in the format 'owner/repo'
  -t, --token=<token>        GitHub personal access token. If not provided, will
                               look for GITHUB_TOKEN environment variable.
      --version              Show version info and exit.
```

## Input File Formats

### JSON Format

```json
[
  {
    "title": "Issue Title",
    "body": "Issue description and details",
    "labels": "label1,label2",
    "assignees": "username1,username2"
  },
  {
    "title": "Another Issue",
    "body": "Description for another issue",
    "labels": "bug",
    "assignees": ""
  }
]
```

### CSV Format

```csv
title,body,labels,assignees
"Issue Title","Issue description and details","label1,label2","username1,username2"
"Another Issue","Description for another issue","bug",""
```

## GitHub Token

Create a Personal Access Token with `repo` scope at [GitHub Personal Access Tokens](https://github.com/settings/tokens).

## Examples

### Create issues from a JSON file

```bash
java -jar bulk-github-issue-creator.jar --repo yourusername/your-repository --file issues.json
```

### Perform a dry run

```bash
java -jar bulk-github-issue-creator.jar --repo yourusername/your-repository --file issues.csv --dry-run
```

### Force file format

```bash
java -jar bulk-github-issue-creator.jar --repo yourusername/your-repository --file issues.txt --format json
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
