# Entities & Game Objects

How to create something that lives in the world, updates, and renders.

## The contracts

Three small interfaces compose to define "a thing in the game":

```java
public interface GameObject {
    void update(UpdateContext context);
    boolean isActive();
}

public interface Collidable {
    Bounds getBounds();
}

public interface Renderable {
    void render(Graphics2D g2);
    default RenderLayer getRenderLayer() { return RenderLayer.ENTITIES; }
}
```

`Entity` (abstract, in `entity/`) implements all three at once and gives you position, velocity, width/height, and an `active` flag for free:

```java
public abstract class Entity implements GameObject, Collidable, Renderable {
    protected Entity(Vector2 position, float width, float height) { ... }
    // update() already moves position by velocity
    // getBounds() already derives from position + width/height
    // isActive()/markInactive() already wired
}
```

For almost everything you build during the jam, **extend `Entity`** rather than implementing the three interfaces directly. You only need to implement `render(Graphics2D g2)` yourself — `update()`, `getBounds()`, and `isActive()` are already handled, and you can override `update()` if you need more than linear movement.

## Creating a new entity

Minimal example — a player that draws a sprite at its position:

```java
package com.lobsterchops.brainlessgamejam.entity.player;

import java.awt.Graphics2D;
import com.lobsterchops.brainlessgamejam.entity.Entity;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.gfx.Sprite;
import com.lobsterchops.brainlessgamejam.math.Vector2;

public class Player extends Entity {

    private final Sprite sprite;

    public Player(Vector2 position, Sprite sprite) {
        super(position, sprite.getWidth(), sprite.getHeight());
        this.sprite = sprite;
    }

    @Override
    public void update(UpdateContext context) {
        super.update(context); // applies velocity to position
        // your gameplay logic here
    }

    @Override
    public void render(Graphics2D g2) {
        sprite.draw(g2, getPosition());
    }
}
```

If this entity should render in a different pass than normal gameplay objects (e.g. a UI element), override `getRenderLayer()`:

```java
@Override
public RenderLayer getRenderLayer() { return RenderLayer.DEBUG; }
```

(Realistically you'd want a `UI` layer for that rather than piggybacking on `DEBUG` — see `RENDERING.md` for how to add one.)

## Registering an entity with the world

```java
GameSystem gameSystem = ServiceLocator.resolve(GameSystem.class);
gameSystem.addObject(new Player(new Vector2(100, 100), playerSprite));
```

**Important current gap:** `GameSystem.addObject()` puts the object into `pendingObjects`, and `flushPendingObjects()` is called at the start and end of every `update()` — but `flushPendingObjects()` itself is an empty stub right now. It never actually moves anything from `pendingObjects` into the live `objects` list. Until that's implemented, `addObject()` has no visible effect. This is the first thing to fill in before you can spawn anything.

A correct implementation would look like:

```java
private void flushPendingObjects() {
    if (!pendingObjects.isEmpty()) {
        objects.addAll(pendingObjects);
        pendingObjects.clear();
    }
}
```

## The update contract

`GameSystem.update()` only does anything when `state == GameState.PLAYING`. Each call:

1. `beginUpdate()` — flushes pending objects (see gap above).
2. `updateTime()` — increments `tick`, adds a fixed slice to `elapsedMillis`.
3. `updateMetaSystems()` — currently empty; intended spot for systems that aren't per-object (spawners, wave timers, etc.).
4. Builds an `UpdateContext` via `UpdateContext.fixed(this, tick, elapsedMillis)`.
5. `updateObjects(context)` — calls `update(context)` on every **active** object.
6. `updateSystems(context)` — currently empty; intended spot for collision detection, death handling, etc.
7. `endUpdate()` — flushes pending objects again, then `removeInactiveObjects()` (filters out anything where `isActive()` is now false).

`UpdateContext` is a record: `gameSystem`, `tick`, `elapsedMillis`, `fixedDeltaSeconds` (always `1f / TARGET_FPS`, currently ~0.0167s). Use `fixedDeltaSeconds` for any time-based movement math instead of hardcoding `1/60f` yourself.

## Removing an entity

Call `markInactive()` on the entity (inherited from `Entity`). It will be filtered out of `objects` at the end of the next `GameSystem.update()` pass via `removeInactiveObjects()`. There's no immediate/forced removal — it's always end-of-tick.

```java
if (health <= 0) {
    markInactive();
}
```

## Querying the world

```java
List<GameObject> all = gameSystem.getObjects();              // unmodifiable
List<Renderable> renderables = gameSystem.getRenderableObjects(); // active + Renderable instanceof
```

Both are read-only snapshots built fresh on each call (the second one specifically filters to active objects that are also `Renderable`, which all `Entity` subclasses are).

## Collision

`Collidable.getBounds()` is implemented for you by `Entity` via `Bounds.fromCenter(position, width, height)`. `Bounds` is an immutable record with `intersects(Bounds)` and `contains(Vector2)`. There's no broad-phase collision system wired up yet — `GameSystem.updateSystems()` is the stub where that would go, likely as a simple O(n²) pairwise check over `getObjects()` filtered to `Collidable` for a jam-scale entity count.