package com.lobsterchops.brainlessgamejam.config;

import java.awt.Color;

public final class ColorConfig {

	// Base
	public static final Color BLACK = new Color(0, 0, 0);
	public static final Color WHITE = new Color(255, 255, 255);
	public static final Color DARK_GREY = new Color(30, 30, 30);
	
	// Arena
	public static final Color ARENA_BORDER = new Color(80, 80, 80);
	public static final Color ARENA_BACKGROUND = new Color(15, 15, 15);
	
	// Player
	public static final Color PLAYER_COLOR = new Color(255, 255, 255);
	
	// Swarm
	public static final Color FRIENDLY = new Color(80, 200, 120);
	public static final Color IMPOSTER = new Color(220, 60, 60);
	
	// Trail
	public static final Color TRAIL_FRIENDLY = new Color(80, 200, 120, 180);
	public static final Color TRAIL_IMPOSTER = new Color(220, 60, 60, 180);
	
	// Debug
	public static final Color DEBUG_BG     = new Color(0, 0, 0, 120);
    public static final Color DEBUG_TEXT   = new Color(255, 255, 255);
    public static final Color DEBUG_LABEL  = new Color(170, 170, 170);
    public static final Color DEBUG_SHADOW = new Color(0, 0, 0, 180);
	
	private ColorConfig() {
	}

}
