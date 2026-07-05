package com.lobsterchops.brainlessgamejam.entity;


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
 
import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.event.CrossingCompleted;
import com.lobsterchops.brainlessgamejam.event.EntityDestroyed;
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.graphics.AssetManager;
import com.lobsterchops.brainlessgamejam.graphics.Gfx;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.math.Bounds;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.RenderLayer;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
import com.lobsterchops.brainlessgamejam.world.TileMap;
import com.lobsterchops.brainlessgamejam.world.TileType;
 
public class SlimeParent extends Entity {
 
    private static final float SIZE  = 32f;
    private static final float SPEED = 3f;
 
    private static final int DRAW_WIDTH  = 64;
    private static final int DRAW_HEIGHT = 32;
 
    static final int MAX_HISTORY = (SlimeChild.NUM_CHILDREN + 1) * SlimeChild.DELAY;
 
    // The Y threshold below which the parent is considered to have crossed safely.
    // One full tile from the top of the map is enough margin.
    private static final float CROSSING_THRESHOLD_Y = TileMap.TILE_SIZE;
 
    private static final Map<Direction, BufferedImage> SPRITES = Map.of(
        Direction.DOWN,  AssetManager.get(Gfx.SLIME_PARENT_DOWN,  DRAW_WIDTH, DRAW_HEIGHT),
        Direction.UP,    AssetManager.get(Gfx.SLIME_PARENT_UP,    DRAW_WIDTH, DRAW_HEIGHT),
        Direction.LEFT,  AssetManager.get(Gfx.SLIME_PARENT_LEFT,  DRAW_WIDTH, DRAW_HEIGHT),
        Direction.RIGHT, AssetManager.get(Gfx.SLIME_PARENT_RIGHT, DRAW_WIDTH, DRAW_HEIGHT)
    );
 
    private Direction facing = Direction.DOWN;
 
    /** Breadcrumb trail written each tick, read by SlimeChild instances. */
    private final LinkedList<Vector2> positionHistory = new LinkedList<>();
 
    /**
     * Prevents CrossingCompleted from firing more than once per wave.
     * Reset by WaveManager via {@link #resetCrossingGuard()} when the
     * parent is repositioned for the next wave.
     */
    private boolean hasCrossed = false;
 
    public SlimeParent(Vector2 position) {
        super(position, SIZE, SIZE);
        
        GameSystem gameSystem = ServiceLocator.resolve(GameSystem.class);
        for (int i = 0; i < SlimeChild.NUM_CHILDREN; i++) {
			gameSystem.addObject(new SlimeChild(i, positionHistory));
		}
    }
 
    public LinkedList<Vector2> getPositionHistory() {
        return positionHistory;
    }
 
    /**
     * Called by WaveManager after repositioning the parent for the next wave.
     * Allows the crossing detector to fire again.
     */
    public void resetCrossingGuard() {
        hasCrossed = false;
    }
 
    @Override
    public void update(UpdateContext context) {
        InputManager input = ServiceLocator.resolve(InputManager.class);
        Vector2 direction = input.movementDirection();
        setVelocity(direction.multiply(SPEED));
        updateFacing(direction);
        super.update(context); // applies velocity → position
 
        clampToWorld();
        applyLogRiding(context);
        checkWater(context);
        checkCrossing(context);
 
        // Record position AFTER clamping so children never chase an out-of-bounds point
        positionHistory.addFirst(getPosition());
        while (positionHistory.size() > MAX_HISTORY) {
            positionHistory.removeLast();
        }
    }

 
    /**
     * Fires {@link CrossingCompleted} once when the parent reaches the top row.
     * The {@code allAlive} flag compares living children against the wave's
     * starting count — supplied by counting SlimeChild objects in GameSystem.
     */
    private void checkCrossing(UpdateContext context) {
        if (hasCrossed) return;
        if (getPosition().y() >= CROSSING_THRESHOLD_Y) return;
 
        hasCrossed = true;
 
        int childrenAlive = countLivingChildren(context.gameSystem());
 
        // "All alive" means every child is still present — WaveManager set
        // waveStartChildCount, but we don't have a direct reference here.
        // We compare against NUM_CHILDREN (the chain always starts full).
        boolean allAlive = childrenAlive == SlimeChild.NUM_CHILDREN;
 
        EventBus eventBus = ServiceLocator.resolve(EventBus.class);
        eventBus.publish(new CrossingCompleted(childrenAlive, allAlive));
    }
 
    private int countLivingChildren(GameSystem gameSystem) {
        return (int) gameSystem.getObjects().stream()
                .filter(o -> o instanceof SlimeChild && o.isActive())
                .count();
    }
 
    private void clampToWorld() {
        TileMap tileMap = ServiceLocator.resolve(TileMap.class);
        float halfW = SIZE / 2f;
        float halfH = SIZE / 2f;
        float minX = halfW;
        float minY = halfH;
        float maxX = tileMap.worldWidth()  - halfW;
        float maxY = tileMap.worldHeight() - halfH;
        setPosition(getPosition().clamp(minX, minY, maxX, maxY));
    }
 
    private Log applyLogRiding(UpdateContext context) {
        Log ridden = findRiddenLog(context);
        if (ridden != null) {
            Vector2 pos = getPosition();
            setPosition(new Vector2(pos.x() + ridden.getSpeed(), pos.y()));
        }
        return ridden;
    }
 
    private void checkWater(UpdateContext context) {
        if (!isInWater()) return;
        if (findRiddenLog(context) != null) return;
 
        EventBus eventBus = ServiceLocator.resolve(EventBus.class);
        eventBus.publish(new EntityDestroyed(this));
    }
 
    private Log findRiddenLog(UpdateContext context) {
        Bounds myBounds = getBounds();
        List<GameObject> objects = context.gameSystem().getObjects();
        for (GameObject obj : objects) {
            if (obj instanceof Log log && log.isActive()) {
                if (log.getBounds().intersects(myBounds)) {
                    return log;
                }
            }
        }
        return null;
    }
 
    private boolean isInWater() {
        TileMap tileMap = ServiceLocator.resolve(TileMap.class);
        int col = (int) (getPosition().x() / TileMap.TILE_SIZE);
        int row = (int) (getPosition().y() / TileMap.TILE_SIZE);
        return tileMap.get(col, row) == TileType.WATER;
    }
 
    private void updateFacing(Vector2 direction) {
        if (direction.equals(Vector2.ZERO)) return;
 
        if (Math.abs(direction.x()) >= Math.abs(direction.y())) {
            facing = direction.x() > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            facing = direction.y() > 0 ? Direction.DOWN : Direction.UP;
        }
    }
 
    @Override
    public void render(Graphics2D g2) {
        BufferedImage sprite = SPRITES.get(facing);
        if (sprite != null) {
            g2.drawImage(sprite,
                    (int) getPosition().x() - DRAW_WIDTH  / 2,
                    (int) getPosition().y() - DRAW_HEIGHT / 2,
                    null);
        }
    }
 
    @Override
    public RenderLayer getRenderLayer() {
        return RenderLayer.ENTITIES;
    }
}