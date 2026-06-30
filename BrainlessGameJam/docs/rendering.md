# Rendering

How a frame gets drawn, layer by layer.

## Where rendering is triggered from

`GameLoop` calls `requestRepaint.run()` once per simulated frame, which is `GamePanel::repaint`. Swing schedules `paintComponent(Graphics g)` asynchronously from that:

```java
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    ServiceLocator.resolve(RenderPipeline.class).render(g2);
    g2.dispose();
}
```

`RenderPipeline` is the only thing `GamePanel` knows about for drawing — it doesn't reach into `GameSystem` or individual entities directly.

## RenderLayer draw order

```java
public enum RenderLayer {
    BACKGROUND,
    ENTITIES,
    DEBUG;

    public static RenderLayer[] drawOrder() {
        return new RenderLayer[] { BACKGROUND, ENTITIES, DEBUG };
    }
}
```

`drawOrder()` is the single source of truth for paint order. `RenderPipeline.render()` just loops over it:

```java
public void render(Graphics2D g2) {
    for (RenderLayer layer : RenderLayer.drawOrder()) {
        renderLayer(g2, layer);
    }
}
```

To add a new layer (say `UI`, for HUD elements that should draw above entities but below the debug overlay):

1. Add `UI` to the enum and insert it into `DRAW_ORDER` at the position you want.
2. Add a `case UI -> renderUI(g2);` branch in `RenderPipeline.renderLayer()`.
3. Have relevant entities override `getRenderLayer()` to return `RenderLayer.UI`.

You don't need to touch `render()` itself — that's the point of routing through `drawOrder()`.

## What each layer does

**BACKGROUND** — `BackgroundRenderer.render(g2)` fills the whole screen with `ColorConfig.BLACK` using `ScreenConfig.WIDTH`/`HEIGHT`. No camera, no entities, just a flat clear.

**ENTITIES** — `RenderPipeline.renderEntities(g2)` iterates `gameSystem.getRenderableObjects()` and draws anything whose `getRenderLayer() == ENTITIES` (the default for all `Entity` subclasses unless overridden). This is also where the `PAUSED`/`GAME_OVER` overlay stubs live — currently empty `if`/`else if` branches in `GameSystem.getState()`, waiting for a paused-screen and game-over-screen implementation.

**DEBUG** — only runs if `debugMode` is true (toggled by pressing F3, which queues `Command.TOGGLE_DEBUG`, handled in `GameUpdater.processCommands()` → `renderPipeline.toggleDebug()`). Delegates to `DebugRenderer`.

## Drawing a sprite

`Sprite` wraps a `BufferedImage` with an optional origin offset (for centering or anchoring):

```java
BufferedImage img = ResourceLoader.loadImage("/sprites/player.png");
Sprite sprite = new Sprite(img, img.getWidth() / 2, img.getHeight() / 2); // origin = center

// inside an entity's render():
sprite.draw(g2, getPosition()); // Vector2 overload, rounds to int pixel coords
```

There's also a scaled-draw overload: `sprite.draw(g2, x, y, w, h)`.

## Camera — not wired in yet

`Camera` exists in `gfx/` with `follow()`, `clamp()`, `shake()`, and `toScreenX`/`toScreenY` conversion methods, but nothing in `RenderPipeline` or `Sprite.draw()` currently calls into it. Right now, every entity renders at its raw world-space `Vector2` position directly onto screen coordinates — world space and screen space are the same space today.

To wire it in, the entity render path would need to convert through the camera before drawing, e.g.:

```java
// inside RenderPipeline or an entity's render(), once a Camera is available:
int screenX = camera.toScreenX(position.x());
int screenY = camera.toScreenY(position.y());
```

This would mean threading a `Camera` instance into `RenderPipeline` (constructor or `ServiceLocator`) and updating `renderEntities()` to convert positions before calling `renderable.render(g2)` — or having `render(Graphics2D g2)` take a `Camera` parameter so entities do the conversion themselves. Worth deciding which approach before you have more than a couple of entity types, since it changes the `Renderable` interface either way.

## Debug overlay details

`DebugRenderer` draws two columns of text (left: title + FPS, right: OS/Java/CPU/memory/display/game state) using a custom font loaded once as a `static final Font` via `FontLoader.load("/fonts/Mojang-Regular.ttf", 16f)`. Each line gets a translucent background pill (`ColorConfig.DEBUG_BG`) and a 1px drop-shadow (`ColorConfig.DEBUG_SHADOW`) offset by `SHADOW_OFFSET`, drawn before the actual text/label colors.

Lines are built as a `List<DebugLine>` (a private record: `label`, `value`), with `DebugLine.BLANK` used as a spacer. To add a new debug stat, add a line to `buildLeftLines()` or `buildRightLines()`:

```java
new DebugLine("entities", gameSystem.getObjects().size())
```

`DebugMetrics` is currently just an FPS holder (`getFps()`/`setFps()`), populated once per second by `GameLoop.updateFpsIfNeeded()`. If you want more live stats on the overlay (entity count, current scene, etc.), you can either read them straight off `GameSystem` in `DebugRenderer` (as it already does for tick/state/objects), or extend `DebugMetrics` if the value needs to be computed/cached outside the render call.