package com.lobsterchops.brainlessgamejam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BuildInfo {
	
	private static final String COMMIT_HASH = resolveGitHash();
	
	private static final String BUILD_TIME = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	
	private BuildInfo() {
		
	}
	
	public static String getCommitHash() {
		return COMMIT_HASH;
	}
	
	public static String getBuildTime() {
		return BUILD_TIME;
	}
	
	private static String resolveGitHash() {
	    try {
	        ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "--short", "HEAD");
	        Process process = pb.start();

	        try (var reader = new BufferedReader(
	                new InputStreamReader(process.getInputStream()))) {
	            String line = reader.readLine();
	            process.waitFor();
	            return line != null ? line.trim() : "unknown";
	        }

	    } catch (Exception e) {
	        return "unknown";
	    }
	}

}
