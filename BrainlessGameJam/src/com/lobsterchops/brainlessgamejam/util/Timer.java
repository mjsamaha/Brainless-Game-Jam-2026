package com.lobsterchops.brainlessgamejam.util;

public class Timer {
	
	private long duration;
	private long start;
	
	public boolean finished() {
		return System.currentTimeMillis() - start >= duration;
	}

}
