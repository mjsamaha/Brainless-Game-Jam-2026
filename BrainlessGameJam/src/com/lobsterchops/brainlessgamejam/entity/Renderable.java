package com.lobsterchops.brainlessgamejam.entity;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.render.RenderLayer;


public interface Renderable {

	void render(Graphics2D g2);

	default RenderLayer getRenderLayer() {
		return RenderLayer.ENTITIES;
	}
}