package com.lobsterchops.brainlessgamejam.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.lobsterchops.brainlessgamejam.math.Vector2;


public class KeyboardInput implements KeyListener {

    private final EnumMap<InputAction, Boolean> actions =
            new EnumMap<>(InputAction.class);

    private final Queue<Command> commands =
            new LinkedList<>();

    private final Map<Integer, InputAction> movementBindings =
            Map.of(
                    KeyEvent.VK_W, InputAction.MOVE_UP,
                    KeyEvent.VK_S, InputAction.MOVE_DOWN,
                    KeyEvent.VK_A, InputAction.MOVE_LEFT,
                    KeyEvent.VK_D, InputAction.MOVE_RIGHT
            );

    public KeyboardInput() {

        for (InputAction action : InputAction.values()) {
            actions.put(action, false);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();

        InputAction action = movementBindings.get(code);

        if (action != null) {
            actions.put(action, true);
        }

        switch (code) {

            case KeyEvent.VK_ESCAPE ->
                    commands.offer(Command.TOGGLE_PAUSE);

            case KeyEvent.VK_F3 ->
                    commands.offer(Command.TOGGLE_DEBUG);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        InputAction action =
                movementBindings.get(e.getKeyCode());

        if (action != null) {
            actions.put(action, false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public boolean isPressed(InputAction action) {

        return actions.getOrDefault(action, false);
    }

    public Vector2 movementDirection() {

        float x =
                (isPressed(InputAction.MOVE_RIGHT) ? 1 : 0)
                        - (isPressed(InputAction.MOVE_LEFT) ? 1 : 0);

        float y =
                (isPressed(InputAction.MOVE_DOWN) ? 1 : 0)
                        - (isPressed(InputAction.MOVE_UP) ? 1 : 0);

        return new Vector2(x, y).normalized();
    }

    public Command pollCommand() {

        return commands.poll();
    }
}