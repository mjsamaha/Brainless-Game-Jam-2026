# Input & Commands

Two different models live side by side here on purpose: **continuous state** for movement, **one-shot events** for everything else. Knowing which one to reach for matters.

## The two models

**`InputAction`** — continuous, polled state. "Is this key currently held?" Used for movement and anything you need to check every tick (`MOVE_UP`, `MENU_SELECT`, etc.).

```java
public enum InputAction {
    MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT,
    MENU_UP, MENU_DOWN, MENU_LEFT, MENU_RIGHT, MENU_SELECT
}
```

**`Command`** — discrete, queued events. "This thing happened once." Used for key presses that should trigger an action exactly once per press, not repeatedly while held (`TOGGLE_PAUSE`, `TOGGLE_DEBUG`).

```java
public enum Command {
    TOGGLE_PAUSE,
    TOGGLE_DEBUG
}
```

If you're tempted to add `MOVE_UP` logic to the `Command` queue, or poll `TOGGLE_PAUSE` as continuous state, that's the wrong model — it'll either fire every single frame the key is held (commands queued in `keyPressed` won't, since Swing only fires `keyPressed` once per physical press + OS repeat-rate, but it's still the wrong semantic fit) or never reset properly.

## How it flows

```
KeyboardInput implements KeyListener
  keyPressed(e)
    → movementBindings.get(code) → actions.put(action, true)   // continuous
    → switch(code): ESCAPE → commands.offer(TOGGLE_PAUSE)        // one-shot
                    F3     → commands.offer(TOGGLE_DEBUG)

  keyReleased(e)
    → movementBindings.get(code) → actions.put(action, false)
```

`InputManager` is a thin facade over `KeyboardInput` + `MouseInput`, exposing:

```java
inputManager.movementDirection();        // normalized Vector2 from WASD state
inputManager.isPressed(InputAction);     // continuous check
inputManager.pollCommand();              // dequeues one Command, or null
inputManager.getMousePosition();
```

`GameUpdater.processCommands()` is the only place currently draining the command queue:

```java
while ((command = input.pollCommand()) != null) {
    switch (command) {
        case TOGGLE_DEBUG -> renderPipeline.toggleDebug();
        case TOGGLE_PAUSE -> togglePause();
    }
}
```

Entities/gameplay code that needs movement input would call `inputManager.movementDirection()` or `isPressed(InputAction.MOVE_UP)` directly inside their own `update(UpdateContext)` — there's no current example of this in the codebase yet since no gameplay entities exist, but `UpdateContext` doesn't carry `InputManager` through it, so an entity needing input would resolve it via `ServiceLocator.resolve(InputManager.class)` rather than expecting it on the context.

## Adding a new movement binding

Add the key mapping to `movementBindings` in `KeyboardInput`:

```java
private final Map<Integer, InputAction> movementBindings =
        Map.of(
                KeyEvent.VK_W, InputAction.MOVE_UP,
                KeyEvent.VK_S, InputAction.MOVE_DOWN,
                KeyEvent.VK_A, InputAction.MOVE_LEFT,
                KeyEvent.VK_D, InputAction.MOVE_RIGHT,
                KeyEvent.VK_UP, InputAction.MOVE_UP   // arrow key as alt binding
        );
```

`Map.of()` requires unique values per call but allows multiple keys mapping to the same `InputAction`, so arrow keys as WASD alternates work fine.

## Adding a new one-shot command

1. Add the value to `Command`:
   ```java
   public enum Command {
       TOGGLE_PAUSE,
       TOGGLE_DEBUG,
       RESTART_RUN
   }
   ```
2. Wire the key in `KeyboardInput.keyPressed()`'s switch:
   ```java
   case KeyEvent.VK_R -> commands.offer(Command.RESTART_RUN);
   ```
3. Handle it in `GameUpdater.processCommands()`:
   ```java
   case RESTART_RUN -> restartCallback.run();
   ```
   (`GameUpdater` already holds a `restartCallback` — wired from `GameContext` as `this::restartRun` — so this one's nearly free to add.)

## Adding a brand new InputAction

1. Add it to the enum.
2. If it's a movement-style key, add a `movementBindings` entry.
3. If it's not movement (e.g. a menu-only action), you'd need your own binding map and a corresponding `isPressed`-style accessor — `KeyboardInput` currently only has one bindings map (`movementBindings`) feeding the `actions` state, so menu actions (`MENU_UP`, `MENU_SELECT`, etc.) are declared in the enum but have no key bindings wired to them yet. That's a gap to fill in when you build menu navigation.

## Mouse

`MouseInput` only tracks position (`mouseMoved`/`mouseDragged` both update the same `Vector2`). No click handling exists yet — if you need mouse clicks, you'd implement `MouseListener` (not just `MouseMotionListener`) alongside it and likely feed click events into the same `Command` queue model, since a click is a one-shot event just like a key press.