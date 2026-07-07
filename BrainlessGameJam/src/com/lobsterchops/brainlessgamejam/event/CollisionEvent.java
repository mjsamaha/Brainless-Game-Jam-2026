package com.lobsterchops.brainlessgamejam.event;

import com.lobsterchops.brainlessgamejam.entity.GameObject;

public record CollisionEvent(GameObject a, GameObject b) {
}