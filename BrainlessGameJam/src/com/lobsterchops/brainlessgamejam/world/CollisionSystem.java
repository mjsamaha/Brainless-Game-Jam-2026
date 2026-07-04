package com.lobsterchops.brainlessgamejam.world;

import java.util.ArrayList;
import java.util.List;
 
import com.lobsterchops.brainlessgamejam.entity.Collidable;
import com.lobsterchops.brainlessgamejam.entity.GameObject;
import com.lobsterchops.brainlessgamejam.entity.Log;
import com.lobsterchops.brainlessgamejam.event.CollisionEvent;
import com.lobsterchops.brainlessgamejam.event.EventBus;
 
public class CollisionSystem {
 
    private final EventBus eventBus;
 
    public CollisionSystem(EventBus eventBus) {
        this.eventBus = eventBus;
    }
 
    public void resolve(List<GameObject> objects) {
        List<GameObject> collidables = collectCollidables(objects);
        int size = collidables.size();
 
        for (int i = 0; i < size - 1; i++) {
            GameObject a = collidables.get(i);
            Collidable ca = (Collidable) a;
 
            for (int j = i + 1; j < size; j++) {
                GameObject b = collidables.get(j);
                Collidable cb = (Collidable) b;
 
                if (ca.getBounds().intersects(cb.getBounds())) {
                    eventBus.publish(new CollisionEvent(a, b));
                }
            }
        }
    }
 
    private List<GameObject> collectCollidables(List<GameObject> objects) {
        List<GameObject> result = new ArrayList<>();
        for (GameObject obj : objects) {
        	// Exclude logs from collision detection; they are handled separately in SlimeChild.
            if (obj instanceof Log) continue;
            if (obj.isActive() && obj instanceof Collidable) {
                result.add(obj);
            }
        }
        return result;
    }
}
 