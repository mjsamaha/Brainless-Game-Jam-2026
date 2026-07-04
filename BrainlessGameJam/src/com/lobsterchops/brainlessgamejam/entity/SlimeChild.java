package com.lobsterchops.brainlessgamejam.entity;

import com.lobsterchops.brainlessgamejam.math.Vector2;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Map;
 
import com.lobsterchops.brainlessgamejam.graphics.AssetManager;
import com.lobsterchops.brainlessgamejam.graphics.Gfx;
import com.lobsterchops.brainlessgamejam.render.RenderLayer;
 
public class SlimeChild extends Entity {
 
    /** Total number of children in the chain. Used by SlimeParent to size the history buffer. */
    public static final int NUM_CHILDREN = 5;
 
    /** Ticks of delay between each child. Child N tracks mama's position N*DELAY ticks ago. */
    public static final int DELAY = 8;
 
    private static final float SIZE = 24f;
 
    private static final int DRAW_WIDTH  = 48;
    private static final int DRAW_HEIGHT = 24;
 
    private static final Map<Direction, BufferedImage> SPRITES = Map.of(
        Direction.DOWN,  AssetManager.get(Gfx.SLIME_CHILD_DOWN,  DRAW_WIDTH, DRAW_HEIGHT),
        Direction.UP,    AssetManager.get(Gfx.SLIME_CHILD_UP,    DRAW_WIDTH, DRAW_HEIGHT),
        Direction.LEFT,  AssetManager.get(Gfx.SLIME_CHILD_LEFT,  DRAW_WIDTH, DRAW_HEIGHT),
        Direction.RIGHT, AssetManager.get(Gfx.SLIME_CHILD_RIGHT, DRAW_WIDTH, DRAW_HEIGHT)
    );
 
    /** How far back in the history list this child reads (index * DELAY). */
    private final int historyOffset;
 
    /** Shared reference to SlimeParent's position history. */
    private final LinkedList<Vector2> positionHistory;
 
    private Direction facing = Direction.DOWN;
 
    public SlimeChild(int index, LinkedList<Vector2> positionHistory) {
        // Start at ZERO — will snap to correct position once history fills up
        super(Vector2.ZERO, SIZE, SIZE);
        this.historyOffset = (index + 1) * DELAY;
        this.positionHistory = positionHistory;
    }
 
    @Override
    public void update(UpdateContext context) {
        if (positionHistory.size() > historyOffset) {
        	Vector2 target = positionHistory.get(historyOffset);
        	updateFacing(target);;
        	
        	Vector2 curr = getPosition();
        	float lerpSpeed = 0.2f;
        	float x = curr.x() + (target.x() - curr.x()) * lerpSpeed;
        	float y = curr.y() + (target.y() - curr.y()) * lerpSpeed;
        	setPosition(new Vector2(x, y));
        }
    }
 
    private void updateFacing(Vector2 target) {
        Vector2 delta = target.subtract(getPosition());
        if (delta.length() < 0.5f) return; // barely moved — hold last facing
 
        if (Math.abs(delta.x()) >= Math.abs(delta.y())) {
            facing = delta.x() > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            facing = delta.y() > 0 ? Direction.DOWN : Direction.UP;
        }
    }
 
    @Override
    public void render(Graphics2D g2) {
        // Don't render until we have a real position from history
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
    
    public int getHistoryOffset() {
    	return historyOffset;
    }
}
 