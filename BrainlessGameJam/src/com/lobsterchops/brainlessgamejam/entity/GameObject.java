package com.lobsterchops.brainlessgamejam.entity;

public interface GameObject {
	
	void update(UpdateContext context);
	
	boolean isActive();

}
