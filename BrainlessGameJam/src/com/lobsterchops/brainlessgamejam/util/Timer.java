package com.lobsterchops.brainlessgamejam.util;

// Timer class for tracking elapsed time
// Timer is a utility class that allows you to set a duration and check if the timer has finished based on the elapsed time since it was started.
public class Timer {

    private long duration;
    private long start;

    public Timer(long durationMs) {
        this.duration = durationMs;
        this.start = System.currentTimeMillis();
    }

    public boolean finished() {
        return System.currentTimeMillis() - start >= duration;
    }

    public void reset() {
        this.start = System.currentTimeMillis();
    }

    public void reset(long newDurationMs) {
        this.duration = newDurationMs;
        this.start = System.currentTimeMillis();
    }

}