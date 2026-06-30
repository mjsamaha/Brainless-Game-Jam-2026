package com.lobsterchops.brainlessgamejam.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import com.lobsterchops.brainlessgamejam.math.Vector2;

public class MouseInput implements MouseMotionListener {

    private Vector2 mousePosition = Vector2.ZERO;

    @Override
    public void mouseMoved(MouseEvent e) {

        mousePosition =
                new Vector2(e.getX(), e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        mouseMoved(e);
    }

    public Vector2 getMousePosition() {

        return mousePosition;
    }
}
