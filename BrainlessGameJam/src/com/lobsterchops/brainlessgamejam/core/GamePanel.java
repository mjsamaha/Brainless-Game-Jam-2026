package com.lobsterchops.brainlessgamejam.core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import com.lobsterchops.brainlessgamejam.config.ColorConfig;
import com.lobsterchops.brainlessgamejam.config.ScreenConfig;
import com.lobsterchops.brainlessgamejam.input.InputManager;
import com.lobsterchops.brainlessgamejam.render.DebugMetrics;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.scene.GameUpdater;

public class GamePanel extends JPanel implements Runnable {
	
	private static final long serialVersionUID = 1L;

    private Thread gameThread;
    private GameLoop gameLoop;          // built after context bootstraps
    private final GameContext context;  // kept only to call setupNewRun/restartRun
    
    
    public GamePanel() {
        initializePanel();
        this.context = new GameContext();   // registers all services
        this.gameLoop = buildGameLoop(); // builds the game loop after services are registered

      
    }
    
    private void initializePanel() {
        this.setPreferredSize(new Dimension(ScreenConfig.WIDTH, ScreenConfig.HEIGHT));
        this.setBackground(ColorConfig.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
    }
    
    public void setupGame() {
    	ServiceLocator.resolve(InputManager.class).register(this);
        context.setupNewRun();
    }

    public void startGameThread() {
        if (gameThread != null && gameThread.isAlive()) return;
        gameThread = new Thread(this, "game-thread");
        gameThread.start();
    }
    
    public void stopGameThread() {
        gameLoop.stop();
        gameThread = null;
    }
    
    private GameLoop buildGameLoop() {
        GameUpdater updater = ServiceLocator.resolve(GameUpdater.class);
        DebugMetrics debugMetrics = ServiceLocator.resolve(DebugMetrics.class);
        return new GameLoop(updater::update, this::repaint, debugMetrics);
    }
    
    @Override
    public void run() {
        gameLoop.run();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		ServiceLocator.resolve(RenderPipeline.class).render(g2);
		g2.dispose();
    }
}
