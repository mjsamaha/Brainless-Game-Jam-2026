package com.lobsterchops.brainlessgamejam.core;

import java.util.HashMap;
import java.util.Map;

/**
 * <h4>Global service registry.</h4> 
 * <p>Subsystems register themselves once during bootstrap (GameContext) and resolve dependencies as needed.</p>
 * <p>This is a simple implementation of the Service Locator pattern.</p>
 */
public final class ServiceLocator {
	
	private static final Map<Class<?>, Object> services = new HashMap<>();
	
	private ServiceLocator() {
		
	}
	
	public static <T> void register(Class<T> type, T instance) {
		if (services.containsKey(type)) {
			throw new IllegalStateException("Service already registered for type: " + type.getName());
		}
		services.put(type, instance);
	}
	
	
	@SuppressWarnings("unchecked")
    public static <T> T resolve(Class<T> type) {
        T service = (T) services.get(type);
        if (service == null) {
            throw new IllegalStateException(
                "No service registered for: " + type.getName()
            );
        }
        return service;
    }

    /** Call during test teardown or between runs if you re-bootstrap. */
    public static void clear() {
        services.clear();
    }

}