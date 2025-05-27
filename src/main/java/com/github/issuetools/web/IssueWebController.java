package com.github.issuetools.web;

import com.github.issuetools.BulkGitHubIssueCreator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class IssueWebController {
    @GetMapping("/")
    public String index() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("repo") String repo,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "dryRun", required = false) String dryRun,
                                   Model model) throws IOException {
        // Save uploaded file to a temp file
        File tempFile = File.createTempFile("issues", file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        // Get token from environment variable
        String token = System.getenv("GITHUB_TOKEN");
        System.out.println("[DEBUG] GITHUB_TOKEN from env: " + (token == null ? "null" : "***" + token.substring(Math.max(0, token.length() - 4)))) ;
        if (token == null || token.isEmpty()) {
            model.addAttribute("result", "Error: GITHUB_TOKEN environment variable is not set. Please set it on the server and try again.");
            return "upload";
        }
        // Build args for CLI logic
        List<String> argsList = new ArrayList<>();
        argsList.add("--repo");
        argsList.add(repo);
        argsList.add("--file");
        argsList.add(tempFile.getAbsolutePath());
        if (token != null && !token.isEmpty()) {
            argsList.add("--token");
            argsList.add(token);
        }
        if (dryRun != null) {
            argsList.add("--dry-run");
        }
        String[] args = argsList.toArray(new String[0]);
        System.out.println("[DEBUG] CLI args: " + String.join(" ", args));
        // Capture output and error
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream oldOut = System.out;
        java.io.PrintStream oldErr = System.err;
        System.setOut(new java.io.PrintStream(baos));
        System.setErr(new java.io.PrintStream(baos));
        try {
            BulkGitHubIssueCreator.main(args);
        } catch (Exception e) {
            model.addAttribute("result", "Error: " + e.getMessage() + "\n" + baos);
            return "upload";
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
        String output = baos.toString();
        if (output.isBlank()) {
            model.addAttribute("result", "No output was produced. Please check the log file for more details.");
        } else {
            model.addAttribute("result", output);
        }
        return "upload";
    }
}
