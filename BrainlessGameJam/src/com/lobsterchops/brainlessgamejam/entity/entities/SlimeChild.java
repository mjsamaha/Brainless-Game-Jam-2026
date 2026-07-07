package com.lobsterchops.brainlessgamejam.entity.entities;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.Direction;
import com.lobsterchops.brainlessgamejam.entity.Entity;
import com.lobsterchops.brainlessgamejam.entity.GameObject;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.graphics.AssetManager;
import com.lobsterchops.brainlessgamejam.graphics.Gfx;
import com.lobsterchops.brainlessgamejam.math.Bounds;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.RenderLayer;
import com.lobsterchops.brainlessgamejam.world.TileMap;
import com.lobsterchops.brainlessgamejam.world.common.TileType;
 
public class SlimeChild extends Entity {
	
	private boolean isDead = false;
 
    public static final int NUM_CHILDREN = 5;
    public static final int DELAY = 8;
 
    private static final float SIZE = 24f;
    private static final float LERP_SPEED = 0.2f;
 
    private static final int DRAW_WIDTH  = 48;
    private static final int DRAW_HEIGHT = 24;
 
    private static final Map<Direction, BufferedImage> SPRITES = Map.of(
        Direction.DOWN,  AssetManager.get(Gfx.SLIME_CHILD_DOWN,  DRAW_WIDTH, DRAW_HEIGHT),
        Direction.UP,    AssetManager.get(Gfx.SLIME_CHILD_UP,    DRAW_WIDTH, DRAW_HEIGHT),
        Direction.LEFT,  AssetManager.get(Gfx.SLIME_CHILD_LEFT,  DRAW_WIDTH, DRAW_HEIGHT),
        Direction.RIGHT, AssetManager.get(Gfx.SLIME_CHILD_RIGHT, DRAW_WIDTH, DRAW_HEIGHT)
    );
 
    private final int historyOffset;
    private final LinkedList<Vector2> positionHistory;
 
    private Direction facing = Direction.DOWN;
 
    public SlimeChild(int index, LinkedList<Vector2> positionHistory) {
    	super(positionHistory.isEmpty() ? Vector2.ZERO : positionHistory.get(Math.min((index + 1) * DELAY, positionHistory.size() - 1)), SIZE, SIZE);
        this.historyOffset = (index + 1) * DELAY;
        this.positionHistory = positionHistory;
    }
 
    public int getHistoryOffset() {
        return historyOffset;
    }
 
    @Override
    public void update(UpdateContext context) {
        if (positionHistory.size() <= historyOffset) return;
 
        // Lerp toward history target
        Vector2 target  = positionHistory.get(historyOffset);
        Vector2 current = getPosition();
        float x = current.x() + (target.x() - current.x()) * LERP_SPEED;
        float y = current.y() + (target.y() - current.y()) * LERP_SPEED;
        updateFacing(target);
        setPosition(new Vector2(x, y));
 
        applyLogRiding(context);
        checkWater(context);
    }
    
    @Override
    public void render(Graphics2D g2) {
        if (positionHistory.size() <= historyOffset) return;
 
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
 
    /**
     * If standing on a log, ride it horizontally this tick.
     */
    private void applyLogRiding(UpdateContext context) {
        Log ridden = findRiddenLog(context);
        if (ridden != null) {
            Vector2 pos = getPosition();
            setPosition(new Vector2(pos.x() + ridden.getSpeed(), pos.y()));
        }
    }
 
    /**
     * If standing in water with no log, publish EntityDestroyed.
     * PlayingScene's loseLastChild() handles which child actually disappears.
     */
    private void checkWater(UpdateContext context) {
//        if (isDead) return;
//        if (!isInWater()) return;
//        if (findRiddenLog(context) != null) return;
//
//        isDead = true;
//        markInactive();
//        EventBus eventBus = ServiceLocator.resolve(EventBus.class);
//        eventBus.publish(new EntityDestroyed(this));
    }
 
    private Log findRiddenLog(UpdateContext context) {
        Bounds myBounds = getBounds();
        List<GameObject> objects = context.gameSystem().getObjects();
        for (GameObject obj : objects) {
            if (obj instanceof Log log && log.isActive()) {
                if (log.getBounds().intersects(myBounds)) { // original: log.getBounds().intersects(getBounds())
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
 
    private void updateFacing(Vector2 target) {
        Vector2 delta = target.subtract(getPosition());
        if (delta.length() < 0.5f) return;
 
        if (Math.abs(delta.x()) >= Math.abs(delta.y())) {
            facing = delta.x() > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            facing = delta.y() > 0 ? Direction.DOWN : Direction.UP;
        }
    }
 
  
}