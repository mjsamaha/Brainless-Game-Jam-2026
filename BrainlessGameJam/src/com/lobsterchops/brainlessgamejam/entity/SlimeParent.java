package com.lobsterchops.brainlessgamejam.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;

import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.graphics.AssetManager;
import com.lobsterchops.brainlessgamejam.graphics.Gfx;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.RenderLayer;

public class SlimeParent extends Entity {

	private static final float SIZE  = 32f;
    private static final float SPEED = 3f;

    private static final int DRAW_WIDTH  = 64;
    private static final int DRAW_HEIGHT = 32;

    private static final Map<Direction, BufferedImage> SPRITES = Map.of(
        Direction.DOWN,  AssetManager.get(Gfx.SLIME_PARENT_DOWN,  DRAW_WIDTH, DRAW_HEIGHT),
        Direction.UP,    AssetManager.get(Gfx.SLIME_PARENT_UP,    DRAW_WIDTH, DRAW_HEIGHT),
        Direction.LEFT,  AssetManager.get(Gfx.SLIME_PARENT_LEFT,  DRAW_WIDTH, DRAW_HEIGHT),
        Direction.RIGHT, AssetManager.get(Gfx.SLIME_PARENT_RIGHT, DRAW_WIDTH, DRAW_HEIGHT)
    );

    private Direction facing = Direction.DOWN;

    public SlimeParent(Vector2 position) {
        super(position, SIZE, SIZE);
    }


    @Override
    public void update(UpdateContext context) {
        InputManager input = ServiceLocator.resolve(InputManager.class);
        Vector2 direction = input.movementDirection();
        setVelocity(direction.multiply(SPEED));
        updateFacing(direction);
        super.update(context);
    }

    private void updateFacing(Vector2 direction) {
        if (direction.equals(Vector2.ZERO)) return; // no input — hold last facing

        // Favour horizontal or vertical based on whichever axis is stronger
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
