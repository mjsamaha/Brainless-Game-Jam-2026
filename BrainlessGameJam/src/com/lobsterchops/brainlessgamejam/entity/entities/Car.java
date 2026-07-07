package com.lobsterchops.brainlessgamejam.entity.entities;


import java.awt.Color;
import java.awt.Graphics2D;
 
import com.lobsterchops.brainlessgamejam.math.Vector2;
import com.lobsterchops.brainlessgamejam.render.RenderLayer;
import com.lobsterchops.brainlessgamejam.world.TileMap;
import com.lobsterchops.brainlessgamejam.core.ServiceLocator;
import com.lobsterchops.brainlessgamejam.entity.Entity;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
 
public class Car extends Entity {
 
    public static final float WIDTH  = 40f;
    public static final float HEIGHT = 20f;
 
    // Colours cycled by lane index for readability
    private static final Color[] LANE_COLOURS = {
        new Color(200, 50,  50),  
        new Color(50,  50,  200), 
        new Color(180, 100, 20),  
        new Color(160, 40,  160), 
        new Color(40,  160, 80)
    };
 
    private final float speed;   // pixels per tick — negative = moving left
    private final Color colour;
 
    public Car(Vector2 position, float speed, int laneIndex) {
        super(position, WIDTH, HEIGHT);
        this.speed = speed;
        this.colour = LANE_COLOURS[Math.abs(laneIndex) % LANE_COLOURS.length];
    }
 
    @Override
    public void update(UpdateContext context) {
        setVelocity(new Vector2(speed, 0));
        super.update(context); // applies velocity → position
        wrapAroundWorld();
    }

 
    @Override
    public void render(Graphics2D g2) {
        int drawX = (int) (getPosition().x() - WIDTH  / 2f);
        int drawY = (int) (getPosition().y() - HEIGHT / 2f);
 
        // Body
        g2.setColor(colour);
        g2.fillRect(drawX, drawY, (int) WIDTH, (int) HEIGHT);
 
        // Dark outline for readability against road
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
 
        // Moving right — wrap when fully off the right edge
        if (speed > 0 && x - WIDTH / 2f > worldWidth) {
            setPosition(new Vector2(-WIDTH / 2f, y));
        }
        // Moving left — wrap when fully off the left edge
        else if (speed < 0 && x + WIDTH / 2f < 0) {
            setPosition(new Vector2(worldWidth + WIDTH / 2f, y));
        }
    }
}