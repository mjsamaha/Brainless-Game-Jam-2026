package com.lobsterchops.brainlessgamejam.entity.entities;


import java.awt.Color;
import java.awt.Graphics2D;
 
import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.Entity;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.RenderLayer;
import com.lobsterchops.brainlessgamejam.world.TileMap;
 
public class Log extends Entity {
 
    public static final float WIDTH  = 80f;
    public static final float HEIGHT = 24f;
 
    private static final Color LOG_BROWN = new Color(139, 90, 43);
    private static final Color LOG_DARK  = new Color(101, 67, 33);
 
    private final float speed; // pixels per tick — negative = moving left
 
    public Log(Vector2 position, float speed) {
        super(position, WIDTH, HEIGHT);
        this.speed = speed;
    }
 
    /** Exposes horizontal speed so riders can add it to their own position. */
    public float getSpeed() {
        return speed;
    }
 
    @Override
    public void update(UpdateContext context) {
        setVelocity(new Vector2(speed, 0));
        super.update(context);
        wrapAroundWorld();
    }
 
    @Override
    public void render(Graphics2D g2) {
        int drawX = (int) (getPosition().x() - WIDTH  / 2f);
        int drawY = (int) (getPosition().y() - HEIGHT / 2f);
 
        // Main body
        g2.setColor(LOG_BROWN);
        g2.fillRect(drawX, drawY, (int) WIDTH, (int) HEIGHT);
 
        // Dark end caps to suggest a cut log
        g2.setColor(LOG_DARK);
        g2.fillRect(drawX,                    drawY, 8, (int) HEIGHT);
        g2.fillRect(drawX + (int) WIDTH - 8,  drawY, 8, (int) HEIGHT);
 
        // Outline
        g2.setColor(Color.BLACK);
        g2.drawRect(drawX, drawY, (int) WIDTH, (int) HEIGHT);
    }
 
    @Override
    public RenderLayer getRenderLayer() {
        return RenderLayer.ENTITIES;
    }
    
    private void wrapAroundWorld() {
        TileMap tileMap = ServiceLocator.resolve(TileMap.class);
        float worldWidth = tileMap.worldWidth();
        float x = getPosition().x();
        float y = getPosition().y();
 
        if (speed > 0 && x - WIDTH / 2f > worldWidth) {
            setPosition(new Vector2(-WIDTH / 2f, y));
        } else if (speed < 0 && x + WIDTH / 2f < 0) {
            setPosition(new Vector2(worldWidth + WIDTH / 2f, y));
        }
    }
}
 