package com.lobsterchops.brainlessgamejam.world;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.math.Vector2;

public class TrailSystem {
	
	private static final int SEGMENT_PACING = 8;
	
	private final Deque<Vector2> positionHistory = new ArrayDeque<>();
	
	private final List<Color> segmentColors = new ArrayList<>();
	
	private int segmentCount = 0;
	
	public void record(Vector2 pos) {
		positionHistory.addFirst(pos);
		trimHistory();
	}
	
	public void attach(boolean friendly) {
		segmentCount++;
		segmentColors.add(0, friendly ? ColorConfig.TRAIL_FRIENDLY : ColorConfig.TRAIL_IMPOSTER);
		trimHistory();
	}
	
	public void detach() {
		if (segmentCount == 0) return;
		segmentCount--;
		if (!segmentColors.isEmpty()) {
			segmentColors.remove(0);
		}
	}
	
	public Vector2 getSegmentPosition(int index) {
		int historyIndex = (index + 1) * SEGMENT_PACING;
		if (historyIndex >= positionHistory.size()) return null;
		
		int i = 0;
		for (Vector2 v : positionHistory) {
			if (i == historyIndex) {
				return v;
			}
			i++;
		}
		return null;
	}
	
	public Color getSegmentColor(int index) {
		if (index >= segmentColors.size()) return ColorConfig.TRAIL_FRIENDLY;
		return segmentColors.get(index);
	}
	
	public int getSegmentCount() {
		return segmentCount;
	}
	
	public void clear() {
		positionHistory.clear();
		segmentColors.clear();
		segmentCount = 0;
	}
	
	private void trimHistory() {
		int maxHistory = (segmentCount + 1) * SEGMENT_PACING;
		while (positionHistory.size() > maxHistory) {
			positionHistory.removeLast();
		}
	}

}
