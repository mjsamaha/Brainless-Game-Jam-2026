package com.lobsterchops.brainlessgamejam.util;

// Timer class for tracking elapsed time
public class Timer {
	
	private long duration;
	private long start;
	
	public boolean finished() {
		return System.currentTimeMillis() - start >= duration;
	}

}
