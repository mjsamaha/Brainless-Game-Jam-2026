package com.lobsterchops.brainlessgamejam.render;

public enum RenderLayer {
	
	BACKGROUND,
	
	ENTITIES,
	
	DEBUG;
	
	private static final RenderLayer[] DRAW_ORDER = { BACKGROUND, ENTITIES, DEBUG };
	
	public static RenderLayer[] getDrawOrder() {
		return DRAW_ORDER;
	}	
}
