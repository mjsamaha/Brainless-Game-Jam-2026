package com.lobsterchops.brainlessgamejam;

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
			Process process = Runtime.getRuntime().exec("git rev-parse --short HEAD");
			process.waitFor();
			byte[] output = process.getInputStream().readAllBytes();
			
			return new String(output).trim();
		} catch (Exception e) {
			return "unknown";
		}
	}

}
