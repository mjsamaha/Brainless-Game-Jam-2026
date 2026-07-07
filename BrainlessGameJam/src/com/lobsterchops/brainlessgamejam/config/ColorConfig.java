package com.lobsterchops.brainlessgamejam.config;

import java.awt.Color;

public final class ColorConfig {
	 
    // Base
    public static final Color BLACK     = new Color(0, 0, 0);
    public static final Color WHITE     = new Color(255, 255, 255);
    public static final Color DARK_GREY = new Color(30, 30, 30);
 
    // World tiles
    public static final Color GRASS     = new Color(86,  130,   3);
    public static final Color GRASS_ALT = new Color(100, 145,  10);
    public static final Color ROAD      = new Color(80,   80,  80);
    public static final Color ROAD_LINE = new Color(200, 180,   0);
    public static final Color WATER     = new Color(40,  100, 200);
    public static final Color WATER_ALT = new Color(30,   85, 180);
 
    // HUD — top bar
    public static final Color HUD_BAR_BG       = new Color( 10,  10,  16, 210);
    public static final Color HUD_BAR_BORDER   = new Color(255, 255, 255,  25);
 
    // HUD — score / wave labels & values
    public static final Color HUD_SCORE_VALUE  = new Color(255, 235, 100);   // warm gold
    public static final Color HUD_SCORE_LABEL  = new Color(180, 160,  80);   // muted gold
    public static final Color HUD_WAVE_VALUE   = new Color(120, 210, 255);   // cool blue
    public static final Color HUD_WAVE_LABEL   = new Color( 80, 150, 190);
 
    // HUD — child silhouette dots
    public static final Color HUD_DOT_ALIVE      = new Color( 90, 200,  80); // slime green
    public static final Color HUD_DOT_ALIVE_GLOW = new Color(140, 255, 120,  80);
    public static final Color HUD_DOT_DEAD       = new Color( 55,  55,  55);
    public static final Color HUD_DOT_DEAD_RING  = new Color( 80,  80,  80);
 
    // HUD — inter-wave banner
    public static final Color HUD_BANNER_BG      = new Color(  0,   0,   0, 170);
    public static final Color HUD_BANNER_TITLE   = new Color(255, 235, 100);
    public static final Color HUD_BANNER_PERFECT = new Color(100, 255, 130);
    public static final Color HUD_BANNER_DELTA   = new Color(220, 220, 220);
 
    // Debug overlay
    public static final Color DEBUG_BG     = new Color(0, 0, 0, 120);
    public static final Color DEBUG_TEXT   = new Color(255, 255, 255);
    public static final Color DEBUG_LABEL  = new Color(170, 170, 170);
    public static final Color DEBUG_SHADOW = new Color(0, 0, 0, 180);
 
    private ColorConfig() {
    }
}
 