package com.lobsterchops.brainlessgamejam.input;

import java.awt.Component;

import com.lobsterchops.brainlessgamejam.math.Vector2;


public class InputManager {

    private final KeyboardInput keyboard =
            new KeyboardInput();

    private final MouseInput mouse =
            new MouseInput();

    public void register(Component component) {

        component.addKeyListener(keyboard);

        component.addMouseMotionListener(mouse);

        component.setFocusable(true);

        component.requestFocusInWindow();
    }

    public Vector2 movementDirection() {

        return keyboard.movementDirection();
    }

    public Vector2 getMousePosition() {

        return mouse.getMousePosition();
    }

    public boolean isPressed(InputAction action) {

        return keyboard.isPressed(action);
    }

    public Command pollCommand() {

        return keyboard.pollCommand();
    }
}