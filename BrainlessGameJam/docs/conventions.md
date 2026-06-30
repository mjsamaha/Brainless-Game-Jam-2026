# Conventions & Known Gaps

Patterns to follow as you keep building, and a running list of stubs/gaps so they don't get lost mid-jam.

## Config classes

Hardcoded values belong in a `config/` class, not scattered through gameplay code. Existing pattern:

```java
public final class GameLoopConfig {
    public static final int TARGET_FPS = 60;
    private GameLoopConfig() {}
}
```

All-static, `final` class, private no-arg constructor to block instantiation. Group related constants together (`ColorConfig` for colors, `ScreenConfig` for screen dimensions, `GameLoopConfig` for timing/difficulty tuning). If you're about to write a literal number or color directly in an entity or system class, stop and ask whether it belongs in an existing config class or a new one — `GameLoopConfig` already has jam-relevant scaffolding waiting (`SPAWN_RATE_SCALE_PER_WAVE`, `WAVES_PER_ACT`, `ENDLESS_DIFFICULTY_SCALE`) that nothing currently reads yet.

## ServiceLocator usage

- Register once per type, in `GameContext`'s constructor, in dependency order (a service that needs another must be constructed after it).
- Resolve anywhere via `ServiceLocator.resolve(Type.class)` — don't pass services through long constructor chains if `ServiceLocator` already makes them reachable; that's the tradeoff this pattern is making.
- There is no `unregister` — only a full `clear()`. Don't rely on swapping a single service at runtime without a larger refactor.
- `resolve()` throws `IllegalStateException` if nothing's registered — there's no silent-null fallback, which is good for catching bootstrap-order mistakes early, but means a missing registration is a hard crash, not a soft failure.

## Package dependency flow

The intended direction (matching what currently exists):

```
core → (everything else)
scene → world, input, render, audio
render → entity, world, state
world → entity, config, state
entity → math, render (RenderLayer only), config
gfx → math, config
audio → (self-contained)
config, math, state → (no dependencies on other packages)
```

`config`, `math`, and `state` should stay leaf packages — nothing in them should ever import from `entity`, `render`, `world`, etc. If you find yourself importing `world.GameSystem` into `config`, that's a sign the value being added doesn't actually belong in a config class.

## Naming

- Records for immutable value types (`Vector2`, `Bounds`, `UpdateContext`, `SoundDefinition`).
- Interfaces for behavioral contracts (`GameObject`, `Renderable`, `Collidable`, `AudioService`).
- `XConfig` for static constant holders.
- `XManager` for facades coordinating sub-components (`InputManager` over keyboard/mouse).
- `XSystem` for a domain's runtime owner (`GameSystem`).
- `XService` for swappable-implementation infrastructure (`AudioService` / `JavaSoundAudioService`).

## Known gaps (current as of this doc set)

These aren't bugs exactly — they're stubs left for you to fill in. Listed here so they don't get rediscovered by surprise later.

1. **`GameSystem.flushPendingObjects()` is empty.** `addObject()` currently has no real effect because pending objects never get moved into the live `objects` list. This blocks spawning anything. See `ENTITIES_AND_OBJECTS.md` for the one-line fix.
2. **`GameSystem.updateMetaSystems()` and `updateSystems()` are empty.** Intended homes for wave/spawn timers and collision/death handling respectively.
3. **`Timer.java` has no way to set `duration` or `start`.** No constructor, no setters — `finished()` will always compare against `0L` for both fields. Needs a constructor (or builder/factory) before it's usable.
4. **`Camera` isn't wired into rendering.** `toScreenX`/`toScreenY` exist but nothing calls them; entities render in raw world coordinates. See `RENDERING.md` for the wiring options.
5. **`RenderPipeline`'s `PAUSED`/`GAME_OVER` branches are empty.** No paused-screen or game-over-screen rendering yet.
6. **Menu `InputAction`s have no key bindings.** `MENU_UP`/`MENU_DOWN`/`MENU_LEFT`/`MENU_RIGHT`/`MENU_SELECT` exist in the enum but `KeyboardInput` only binds movement keys.
7. **No click handling in `MouseInput`.** Only position tracking (`MouseMotionListener`) — no `MouseListener` for clicks yet.
8. **`AudioCatalog` has zero sounds registered.** The registration calls are commented out as an example; nothing plays until real assets + `register()` calls are added.
9. **`gfx` package name is inconsistent** with the rest of the singular-domain naming (`render`, `world`, `entity`). Not urgent, just a heads-up if you go looking for `Camera`/`Sprite` and check `render/` first out of habit.

When you close one of these out, it's worth deleting the corresponding line here so the list stays an accurate "what's still open" snapshot rather than a permanent changelog.