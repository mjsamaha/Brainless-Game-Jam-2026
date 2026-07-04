package com.lobsterchops.brainlessgamejam.core;

import javax.swing.JFrame;

import com.lobsterchops.brainlessgamejam.Version;

public class Game {

	private final JFrame window;
	private final GamePanel gamePanel;

	public Game() {
		this.window = buildWindow();
		this.gamePanel = new GamePanel();

		window.setContentPane(gamePanel);
		window.pack();
		window.setLocationRelativeTo(null);
	}

	public void start() {
		window.setVisible(true);
		gamePanel.setupGame();
		gamePanel.startGameThread();
	}

	private static JFrame buildWindow() {
	    JFrame frame = new JFrame();
	    frame.setTitle(Version.getWindowTitle());
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setResizable(false);
	    return frame;
	}

}
