package com.lobsterchops.brainlessgamejam.core;

import com.lobsterchops.brainlessgamejam.config.GameLoopConfig;
import com.lobsterchops.brainlessgamejam.render.DebugMetrics;
import com.lobsterchops.brainlessgamejam.util.FpsCounter;

public class GameLoop {
	
	private final Runnable updateTick;
	private final Runnable requestRepaint;
	
	private final DebugMetrics debugMetrics;
	private final FpsCounter fpsCounter = new FpsCounter();
	
	private volatile boolean running = true;
	
	public GameLoop(Runnable updateTick, Runnable requestRepaint, DebugMetrics debugMetrics) {
		this.updateTick = updateTick;
		this.requestRepaint = requestRepaint;
		this.debugMetrics = debugMetrics;
	}
	
	public void run() {
	    double delta = 0.0;
	    long lastTime = System.nanoTime();

	    while (running) {
	        long currentTime = System.nanoTime();
	        long elapsed = currentTime - lastTime;
	        lastTime = currentTime;

	        delta = updateGame(delta, elapsed);
	        updateDebug(elapsed);
	        sleepUntilNextFrame(currentTime);
	    }
	}
	
	private double updateGame(double delta, long elapsed) {
	    delta += calculateDelta(elapsed);

	    processUpdates(delta);

	    return delta % 1;
	}

	private void updateDebug(long elapsed) {
	    fpsCounter.frame(elapsed);
	    updateFpsIfNeeded();
	}

	private void sleepUntilNextFrame(long frameStart) {
	    long frameElapsed = System.nanoTime() - frameStart;
	    long sleepNanos = (long) GameLoopConfig.DRAW_INTERVAL - frameElapsed;

	    if (sleepNanos <= 0) {
	        return;
	    }

	    try {
	        Thread.sleep(
	            sleepNanos / 1_000_000,
	            (int) (sleepNanos % 1_000_000)
	        );
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	    }
	}
	
	private double calculateDelta(long elapsedNanos) {
		return elapsedNanos / GameLoopConfig.DRAW_INTERVAL;
	}
	
	private void processUpdates(double delta) {
		while (delta >= 1) {
			updateTick.run();
			requestRepaint.run();
			delta--;
		}
	}
	
	private void updateFpsIfNeeded() {
	    if (fpsCounter.shouldUpdate()) {
	        int fps = fpsCounter.consumeFps();
	        debugMetrics.setFps(fps);
	        System.out.println(String.format("FPS: %3d", fps));
	    }
	}

	public void stop() {
		running = false;
	}

}
