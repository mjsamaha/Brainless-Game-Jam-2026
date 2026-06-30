package com.lobsterchops.brainlessgamejam.util;

import com.lobsterchops.brainlessgamejam.config.GameLoopConfig;

public class FpsCounter {
	
	private long timer = 0L;
    private int frameCount = 0;

    public void frame(long elapsedNanos) {
        timer += elapsedNanos;
        frameCount++;
    }

    public boolean shouldUpdate() {
        return timer >= GameLoopConfig.TIMER_INTERVAL;
    }

    public int consumeFps() {
        int fps = frameCount;
        frameCount = 0;
        timer = 0L;
        return fps;
    }

}
