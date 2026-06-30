# Architecture

How Brainless Game Jam boots up and runs, end to end.

## Package map

```
com.lobsterchops.brainlessgamejam
├── core      Bootstrap, service registry, game loop, panel/window
├── entity    GameObject/Renderable/Collidable contracts, Entity base class
├── world     GameSystem (the entity list + tick/state owner)
├── render    RenderPipeline, layers, debug overlay
├── input     InputManager, keyboard/mouse, actions, commands
├── audio     AudioService, catalog, sound definitions
├── math      Vector2, Bounds
├── gfx       Camera, Sprite
├── config    Static constants (colors, screen, loop tuning)
├── scene     GameUpdater (per-tick orchestration)
├── state     GameState enum
└── util      FontLoader, ResourceLoader, FpsCounter, Timer
```

`gfx` is the one package name that doesn't match the others (`render`/`world`/`entity` are all singular-noun-as-domain). Not a problem, just worth knowing if you're hunting for `Camera` or `Sprite` and instinctively check `render` first.

## Boot sequence

```
Main.main()
  → new Game()
      → buildWindow()              // JFrame, title from Version
      → new GamePanel()
          → initializePanel()      // size, background, focusable
          → new GameContext()      // <-- all services created + registered here
          → buildGameLoop()        // pulls GameUpdater + DebugMetrics back out
  → game.start()
      → window.setVisible(true)
      → gamePanel.setupGame()      // registers input listeners, calls context.setupNewRun()
      → gamePanel.startGameThread()
```

`GameContext`'s constructor is the single place every long-lived service gets created and registered:

```java
InputManager → GameSystem → DebugMetrics → RenderPipeline → AudioService → GameUpdater
```

Order matters here because `RenderPipeline` needs `GameSystem` + `DebugMetrics` already constructed, and `GameUpdater` needs all four of the others. If you add a new service that depends on an existing one, construct it after its dependency and register both with `ServiceLocator.register()`.

`GamePanel.setupGame()` is a frequent gotcha source — `InputManager.register(this)` must be called explicitly here, or key/mouse listeners are never attached and input silently does nothing. If you ever restructure `GamePanel`, keep this call intact.

## The game loop

`GameLoop.run()` implements a fixed-timestep loop with frame limiting:

1. Measure elapsed nanos since last iteration.
2. Convert elapsed time into a `delta` accumulator, scaled by `GameLoopConfig.DRAW_INTERVAL` (nanos per frame at `TARGET_FPS`, currently 60).
3. While `delta >= 1`, run one full `updateTick.run()` + `requestRepaint.run()`, then decrement `delta` by 1.
4. Feed the real elapsed time into `FpsCounter`, and push the computed FPS into `DebugMetrics` once per second (`TIMER_INTERVAL`).
5. Sleep for whatever time is left in the frame budget (`DRAW_INTERVAL - frameElapsed`), via `Thread.sleep`.

This means: the game *can* run fewer than 60 update/render passes per second if the previous frame took too long (delta won't reach 1), and it will **not** run faster than 60 because of the sleep at the end. Without that sleep, FPS would read arbitrarily high and unstable — frame limiting is not automatic, it's the explicit `Thread.sleep` math at the bottom of `run()`.

`updateTick` and `requestRepaint` are passed into `GameLoop` as plain `Runnable`s from `GamePanel.buildGameLoop()` — they're `GameUpdater::update` and `this::repaint` respectively. The loop itself has zero knowledge of what a "game" is; it just calls two callbacks on a schedule.

## Per-tick orchestration

`GameUpdater.update()` is what `updateTick` actually points to. Every tick, in order:

1. `processCommands()` — drains `InputManager.pollCommand()` and dispatches one-shot actions (`TOGGLE_DEBUG` → `renderPipeline.toggleDebug()`, `TOGGLE_PAUSE` → `togglePause()`).
2. `gameSystem.update()` — advances tick count, elapsed time, and (when `PLAYING`) updates all active game objects.
3. `audioService.update()` — lets the audio service clean up finished clips.

Rendering is **not** triggered from here — it's driven separately by `GamePanel.repaint()`, which Swing schedules to call `paintComponent()`, which calls `RenderPipeline.render()`. Update and render are two separate callbacks invoked back-to-back by `GameLoop`, not one combined step.

## Service access

Everything that isn't constructor-injected goes through `ServiceLocator`:

```java
InputManager input = ServiceLocator.resolve(InputManager.class);
```

It's a flat `Class -> instance` map. `register()` throws if you double-register a type, `resolve()` throws if nothing's registered. There's no `unregister` — only `clear()`, which wipes everything (used for test teardown / full re-bootstrap, not for swapping one service). If you need two instances of the same service type, `ServiceLocator` as written can't do that — you'd need a different registry key or a wrapper.

## See also

- `ENTITIES_AND_OBJECTS.md` — what lives inside `GameSystem` and how to add to it
- `RENDERING.md` — what happens inside `RenderPipeline.render()`
- `INPUT_AND_COMMANDS.md` — `InputAction` vs `Command`
- `CONVENTIONS.md` — known gaps and patterns to follow