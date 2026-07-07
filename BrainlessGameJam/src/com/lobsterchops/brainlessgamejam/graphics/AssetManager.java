package com.lobsterchops.brainlessgamejam.graphics;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.lobsterchops.brainlessgamejam.util.ResourceLoader;

public final class AssetManager {
	
	private static final Map<String, BufferedImage> cache = new HashMap<>();
	
	private AssetManager() {
		
	}
	
	public static BufferedImage get(String path, int width, int height) {
	    String key = path + "@" + width + "x" + height;
	    return cache.computeIfAbsent(key, k -> {
	        BufferedImage src = ResourceLoader.loadImage(path);
	        if (src == null) return null;
	        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D g2 = scaled.createGraphics();
	        g2.setRenderingHint(
	            RenderingHints.KEY_INTERPOLATION,
	            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	        g2.drawImage(src, 0, 0, width, height, null);
	        g2.dispose();
	        return scaled;
	    });
	}
	
	

}
