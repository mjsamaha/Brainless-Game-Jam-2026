# 🦆 Duckling Parade — Implementation Roadmap

> Each phase ends with a **playtest gate** — a set of checks you should be able to verify
> by running the game before moving on. Don't proceed until the gate passes.

---

## Current State Audit

Before any phases begin, here is what the framework already gives you for free:

| Already Done | Notes |
|---|---|
| Game loop (fixed timestep, 60 FPS) | `GameLoop`, `GameLoopConfig` |
| Scene system | `SceneManager`, `Scene`, `PlayingScene`, `PausedScene`, `MenuScene` |
| Pause / unpause (Escape) | `GameUpdater.togglePause()` — fully wired |
| Debug overlay (F3) | `DebugRenderer` — objects, tick, FPS, memory, state |
| Collision detection | `CollisionSystem` — pairwise broadphase, publishes `CollisionEvent` |
| Event bus | `EventBus` — subscribe/publish, clears on restart |
| Entity base class | `Entity` — position, velocity, bounds, `markInactive()` |
| Audio service | `JavaSoundAudioService` — play, pause, resume, volume |
| Input | `InputManager`, `KeyboardInput` — WASD + commands wired |
| Service locator / bootstrap | `GameContext`, `ServiceLocator` |
| Camera | `Camera` — follow, clamp, shake |
| Math | `Vector2`, `Bounds` — full suite |

### Known Gaps to Fix Before Phase 1

These are broken stubs that will block you immediately:

1. **`GameSystem.flushPendingObjects()`** — body is empty. Objects added via `addObject()` never join the live list. Fix first.
2. **`GameSystem.clear()`** — resets time/state but never clears `objects` or `pendingObjects`. Add those clears.
3. **`Timer`** — `duration` and `start` have no constructor, no `reset()`. Add them before using it.
4. **`GameContext.setupNewRun()`** — resolves `InputManager` and `GameSystem` but does nothing with them. This is where you'll spawn initial entities each phase.
5. **`SceneManager` initial scene** — `GameContext` starts on `PlayingScene`, but `GameSystem` starts in `GameState.MENU` state, so `GameSystem.update()` returns immediately. Either start in `PLAYING` state or wire `MenuScene` first. For now, the quickest fix is to change the initial state to `PLAYING` in `GameSystem` or call `gameSystem.clear()` in `setupNewRun()`.

---

## Phase 1 — Mama Duck Moves

**Goal:** A controllable duck appears on screen and moves with WASD. Nothing else.

### What to Build

- **`MamaDuck extends Entity`**
  - Constructor: `(Vector2 position)` — fixed size 32×32.
  - `update(UpdateContext)`: read `context.gameSystem()` → get `InputManager` from `ServiceLocator` → call `movementDirection()` → multiply by speed scalar (e.g. `2.5f`) → `setVelocity(...)`. Call `super.update(context)` to apply velocity to position.
  - `render(Graphics2D g2)`: draw a filled yellow rectangle `g2.fillRect(...)` at screen position for now. No sprite yet.
  - `getRenderLayer()`: return `RenderLayer.ENTITIES` (default, no override needed).

- **Wire into `GameContext.setupNewRun()`**
  - Resolve `GameSystem`, call `gameSystem.clear()` (sets state to PLAYING).
  - Create `new MamaDuck(new Vector2(512, 600))` and call `gameSystem.addObject(duck)`.

- **Fix `GameSystem.flushPendingObjects()`**
  ```java
  private void flushPendingObjects() {
      objects.addAll(pendingObjects);
      pendingObjects.clear();
  }
  ```

- **Fix `GameSystem.clear()`** — add `objects.clear()` and `pendingObjects.clear()`.

### Playtest Gate ✅

- [ ] A yellow rectangle appears near the bottom-centre of the screen.
- [ ] WASD moves it smoothly in all 4 directions.
- [ ] F3 debug overlay shows `objects: 1`, `state: PLAYING`.
- [ ] Escape pauses (screen darkens). Escape again resumes. Duck is frozen while paused.
- [ ] Duck can be driven off-screen edges (no crash — we clamp in a later phase).

---

## Phase 2 — Duckling Follow Chain

**Goal:** 5 ducklings trail behind mama in a conga line.

### What to Build

- **`Duckling extends Entity`**
  - Constructor: `(int index, ArrayDeque<Vector2> positionHistory)` — size 20×20.
  - Each duckling shares the **same** `ArrayDeque<Vector2>` that mama writes to, but reads at offset `index * DELAY` (where `DELAY` is a constant, e.g. `8` ticks).
  - Cleaner alternative: each duckling holds its **own** deque seeded with the target's current position. Each tick: offer leader's current position to front, poll from back to get own target position.
  - `render(Graphics2D g2)`: filled orange/yellow rectangle slightly smaller than mama.

- **Position history approach (recommended)**
  ```
  MamaDuck writes her position to a shared LinkedList<Vector2> each tick (addFirst).
  Duckling N reads positionHistory.get(N * DELAY) — if list is shorter, stay put.
  Keep the list trimmed to MAX_DUCKLINGS * DELAY entries.
  ```

- **Wire in `setupNewRun()`**
  - Create mama, create 5 ducklings, add all to `gameSystem`.
  - Pass mama's position history list into each duckling at construction.

- **Screen boundary clamping for mama**
  - In `MamaDuck.update()`, after `super.update()`, clamp position:
    ```java
    setPosition(getPosition().clamp(16, 16, ScreenConfig.WIDTH - 16, ScreenConfig.HEIGHT - 16));
    ```

### Playtest Gate ✅

- [ ] 5 smaller rectangles follow mama in a chain — each one delayed behind the previous.
- [ ] Turning corners: the chain curves naturally, it doesn't snap.
- [ ] Mama cannot leave the screen edges.
- [ ] F3 shows `objects: 6`.
- [ ] Pause freezes the entire chain.

---

## Phase 3 — Road Hazard (Cars)

**Goal:** Lanes of cars cross the screen. Ducklings can be lost. Game over on mama hit.

### What to Build

- **`Car extends Entity`**
  - Constructor: `(Vector2 position, float speed, int lane)` — size 40×24 or 24×40.
  - `update()`: move horizontally at speed. When fully off-screen edge, wrap to opposite side.
  - `render()`: filled red/blue/green rectangle (vary by lane for readability).

- **`RoadLayout` (plain class, not an Entity)**
  - Defines lane Y-positions and speeds for the current wave.
  - `spawnCars(GameSystem gameSystem)` — creates Car objects for each lane and adds them.
  - Start with 2 lanes for wave 1.

- **Collision handling in `PlayingScene.enter()`**
  ```java
  eventBus.subscribe(CollisionEvent.class, e -> {
      if (e.a() instanceof Car && e.b() instanceof MamaDuck) { triggerGameOver(); }
      if (e.b() instanceof Car && e.a() instanceof MamaDuck) { triggerGameOver(); }
      if (e.a() instanceof Car && e.b() instanceof Duckling d) { loseLastDuckling(); }
      if (e.b() instanceof Car && e.a() instanceof Duckling d) { loseLastDuckling(); }
  });
  ```
  - `loseLastDuckling()`: finds the last active duckling in the chain, calls `markInactive()`, publishes `EntityDestroyed`.
  - `triggerGameOver()`: set `gameSystem.setState(GameState.GAME_OVER)` for now (full scene in Phase 6).

- **`InputAction.FIRE` / `MENU_SELECT`** — wire Enter key → `Command.CONFIRM` if you need scene transitions yet (optional this phase).

### Playtest Gate ✅

- [ ] Cars move across the screen and wrap around.
- [ ] Walking into a car removes the last duckling (it disappears).
- [ ] Walking mama into a car stops the game (state = GAME_OVER, loop stops updating).
- [ ] F3 shows object count rising/falling as ducklings are lost.
- [ ] Pause still works mid-traffic.

---

## Phase 4 — River Hazard & Log Riding

**Goal:** A river zone blocks progress. Logs traverse it. Duck family must ride across.

### What to Build

- **`RiverTile` (zone marker, not collidable)**
  - A rendered strip — filled blue rectangle spanning the screen width.
  - Not an Entity — just a drawn background element in a `TileMap` or drawn directly in a `WorldRenderer`.
  - Alternative: add a `WATER` render layer or render it in `BackgroundRenderer` based on wave layout data.

- **`Log extends Entity`**
  - Size: 64×24. Moves horizontally at slow speed. Wraps off-screen.
  - `render()`: filled brown rectangle.
  - No collidable interaction from `CollisionSystem` — log riding is positional, not collision-based (simpler for jam scope).

- **Log riding logic (in `MamaDuck.update()` and `Duckling.update()`)**
  - Each tick, check if the entity's position overlaps any active Log's bounds.
  - If yes: add the log's velocity to the entity's position this tick (entity rides the log).
  - If the entity is in the river zone but NOT on a log: call `markInactive()` on the rearmost duckling (or game over for mama).
  - Keep a reference to the `GameSystem` in the entity (available via `UpdateContext`) to query log objects.

- **`WorldLayout` (data class)**
  - Defines for the current wave: safe zones, road zones (lane count + speeds), river zones (log count + speed).
  - `setupWave(int wave, GameSystem gameSystem)` — spawns all cars and logs for that wave.

### Playtest Gate ✅

- [ ] A blue strip appears on screen representing the river.
- [ ] Brown logs move across it.
- [ ] Standing mama on a log moves her with it.
- [ ] Mama stepping off a log into water loses a duckling / ends the game.
- [ ] Ducklings also ride logs when following mama onto them.
- [ ] Cars and rivers can coexist on screen.

---

## Phase 5 — Scoring, HUD & Wave Progression

**Goal:** Score counts up. Waves advance when mama crosses. Difficulty increases each wave.

### What to Build

- **`ScoreSystem` (plain class)**
  - Fields: `int score`, `int wave`, `int ducklingsRemaining`.
  - `onDucklingLost()`: decrement `ducklingsRemaining`.
  - `onCrossingComplete()`: `score += ducklingsRemaining * 100` + perfect bonus if all intact.
  - Subscribe to `EntityDestroyed` on `EventBus` — if entity is a `Duckling`, call `onDucklingLost()`.
  - Subscribe to `WaveCompleted` — call `onCrossingComplete()`, increment `wave`.

- **Crossing completion detection**
  - Define a "safe zone" Y threshold at the top of the screen.
  - In `MamaDuck.update()`: when position.y() < CROSSING_THRESHOLD, publish `WaveCompleted(currentWave)`.
  - Or: `WaveManager` checks mama's position each tick and publishes when she crosses.

- **`WaveCompleted` event** — already defined in the codebase, ready to use.

- **`HudRenderer` (new class)**
  - Called from `RenderPipeline.renderEntities()` (or add a `HUD` render layer).
  - Draws: score top-right, wave top-centre, duckling silhouette count top-left.
  - Use `FontLoader` for a clean font. Coloured text via `g2.setColor()`.

- **Wave reset flow**
  - On `WaveCompleted`: wait `BETWEEN_WAVE_DELAY_MS` (3000ms, already in `GameLoopConfig`), then call `WorldLayout.setupWave(newWave, gameSystem)` — which clears old cars/logs and spawns new harder ones.
  - Use `Timer` here (fix Timer first — add constructor `Timer(long durationMs)` and `reset()` method).

- **Difficulty scaling**
  - Each wave: add one more lane (up to max 5), increase car speeds by `SPAWN_RATE_SCALE_PER_WAVE` (1.1f).

### Playtest Gate ✅

- [ ] Score displays on screen and increases when mama crosses with ducklings alive.
- [ ] Wave number increments after each crossing.
- [ ] Cars are visibly faster and more numerous on wave 3+ vs wave 1.
- [ ] Losing a duckling reduces the on-screen silhouette count.
- [ ] A 3-second pause between waves before the next layout spawns.

---

## Phase 6 — Menu & Game Over Scenes

**Goal:** A real title screen and game over screen. Full scene loop works end to end.

### What to Build

- **`MenuScene` (flesh out the stub)**
  - Draw title text "DUCKLING PARADE" centred using `g2.drawString()`.
  - Draw "PRESS ENTER TO START" below.
  - Draw high score if one exists.
  - In `update()`: check `InputManager.isPressed(InputAction.MENU_SELECT)` → switch `SceneManager` to `PlayingScene` and call `gameContext.setupNewRun()`.
  - Wire `Enter` key → `InputAction.MENU_SELECT` in `KeyboardInput.keyPressed()`.

- **`GameOverScene implements Scene`**
  - Constructor takes `ScoreSystem`, `SceneManager`, `GameContext`.
  - `render()`: dim overlay, "GAME OVER", final score, ducklings saved, wave reached, "PRESS ENTER TO RESTART".
  - `update()`: on `MENU_SELECT` → `gameContext.restartRun()` → switch to `PlayingScene`.

- **Wire game over trigger**
  - When `triggerGameOver()` fires (from Phase 3): switch `SceneManager` to `GameOverScene`.
  - Pass `GameOverScene` into `GameUpdater` or handle via `EventBus` (publish a `GameOverEvent`).

- **Fix `GameContext` initial scene**
  - Change `new SceneManager(playingScene)` → `new SceneManager(menuScene)`.
  - `GameSystem` starts in `MENU` state (already the default) so the update loop idles correctly.

- **High score (in-memory for now)**
  - `ScoreSystem` tracks `highScore`. On game over, compare and update.
  - Pass to `MenuScene` and `GameOverScene` for display.

### Playtest Gate ✅

- [ ] Game launches to the menu screen, not directly into gameplay.
- [ ] Pressing Enter starts the game from the menu.
- [ ] Dying (mama hit by car or falls in river) transitions to the game over screen.
- [ ] Game over screen shows correct score and wave.
- [ ] Pressing Enter on game over restarts cleanly — old entities are gone, score resets, wave resets.
- [ ] Pause still works during gameplay. No crash on pause → game over → restart path.

---

## Phase 7 — Sprites & Camera

**Goal:** Replace all placeholder rectangles with sprites. Camera follows mama.

### What to Build

- **Sprites**
  - Load via `ResourceLoader.loadImage("/sprites/mama_duck.png")` → wrap in `Sprite`.
  - 4-directional facing: store `Sprite[] directionFrames` in `MamaDuck`. Pick frame based on last non-zero velocity direction.
  - `Sprite.draw(g2, position)` replaces `g2.fillRect(...)` in each entity's `render()`.

- **Tile rendering**
  - Replace the solid blue river strip and grey road with tiled images.
  - `BackgroundRenderer` or a new `WorldRenderer` draws the tile map before entities.

- **Camera wiring**
  - Add `Camera` to `GameContext`, register in `ServiceLocator`.
  - In `MamaDuck.update()`: `camera.follow(getPosition())`, `camera.clamp(0, 0, worldWidth, worldHeight)`.
  - In every entity's `render()`: convert world position to screen position using `camera.toScreenX() / toScreenY()`.
  - In `RenderPipeline`: pass `Camera` through to entities, or let entities resolve it from `ServiceLocator`.

- **Camera shake on loss**
  - In the `EntityDestroyed` subscriber: if entity is a Duckling, call `camera.shake(200_000_000L, 4f)`.
  - Call `camera.update(elapsedNanos)` in `GameUpdater.update()` or `GameSystem.updateMetaSystems()`.

### Playtest Gate ✅

- [ ] Duck family is drawn with sprites, not rectangles.
- [ ] Camera follows mama — she stays roughly centred as she moves.
- [ ] Losing a duckling causes a brief screen shake.
- [ ] Tiles (road, grass, water) replace solid colour rectangles.
- [ ] No visible rendering artifacts when entities go near camera edges.

---

## Phase 8 — Audio

**Goal:** Music plays. SFX fire on key events.

### What to Build

- **Register assets in `AudioCatalog`'s static block**
  ```java
  register(defs, SoundType.GAMEPLAY_MUSIC,
      SoundDefinition.music("/audio/gameplay.wav", 0.8f));
  register(defs, SoundType.DUCKLING_SPLASH,
      SoundDefinition.sfx("/audio/splash.wav", 1.0f, 3));
  // etc.
  ```

- **Trigger music**
  - Uncomment `audioService.playMusic(SoundType.GAMEPLAY_MUSIC)` in `setupNewRun()`.
  - Play menu music in `MenuScene.enter()`.

- **Trigger SFX via EventBus**
  - In `PlayingScene.enter()`, subscribe:
    - `EntityDestroyed` → if Duckling in river → `audioService.playSfx(DUCKLING_SPLASH)`
    - `EntityDestroyed` → if Duckling hit by car → `audioService.playSfx(DUCKLING_HIT)`
    - `WaveCompleted` → `audioService.playSfx(CROSSING_COMPLETE)`
  - On game over: `audioService.playSfx(GAME_OVER_STING)`, `audioService.stopMusic()`.

- **Pause already handles audio** — `PausedScene.enter()` calls `audioService.pauseAll()`, `exit()` calls `resumeAll()`. No changes needed.

### Playtest Gate ✅

- [ ] Menu music plays on the title screen.
- [ ] Gameplay music starts when a run begins and stops on game over.
- [ ] A splash sound plays when a duckling falls in the river.
- [ ] A hit sound plays when a duckling is struck by a car.
- [ ] A fanfare plays on wave completion.
- [ ] Music pauses when Escape is pressed. Resumes on unpause.

---

## Phase 9 — Polish & Balance

**Goal:** The game feels good. Edge cases are handled. It's ready to submit.

### What to Polish

- **Stray duckling rescue** — spawn a lone `StrayDuckling` entity in the safe zone. On `CollisionEvent` with mama, add it to the chain and award +150 pts.
- **Wave transition screen** — brief overlay between waves showing "WAVE CLEAR!", duckling count, and wave bonus score. Fades out after 2 seconds.
- **Difficulty balancing pass** — play wave 1–10, adjust lane counts, car speeds, log spacing until the curve feels fair but escalating.
- **Screen edge safety** — ensure cars that spawn near the edge don't instantly hit the duck family before they're visible.
- **Chain preservation on restart** — verify `gameSystem.clear()` + `objects.clear()` fully resets the duck chain so a fresh run always starts with exactly 5 ducklings.
- **Persistent high score** — write `highScore` to a local file (`highscore.dat`) on game over. Read it on startup in `GameContext`.
- **README** — controls, how to run the JAR, brief description.

### Final Playtest Gate ✅

- [ ] Full run from menu → gameplay → death → game over → restart works without any crash.
- [ ] High score persists across restarts.
- [ ] Wave 1 is easy enough to be a tutorial. Wave 5+ requires real attention.
- [ ] Pause → resume → die path doesn't corrupt state.
- [ ] F3 debug overlay is useful and shows correct counts throughout.
- [ ] Packaged as a runnable JAR and tested on a clean launch.

---

## Quick Reference — What Gets Added Each Phase

| Phase | New Files | Modified Files |
|---|---|---|
| Pre-work | — | `GameSystem`, `Timer` |
| 1 | `MamaDuck` | `GameContext`, `GameSystem` |
| 2 | `Duckling` | `MamaDuck`, `GameContext` |
| 3 | `Car`, `RoadLayout` | `PlayingScene`, `GameUpdater` |
| 4 | `Log`, `RiverZone`, `WorldLayout` | `MamaDuck`, `Duckling`, `BackgroundRenderer` |
| 5 | `ScoreSystem`, `WaveManager`, `HudRenderer` | `RenderPipeline`, `PlayingScene`, `GameContext`, `Timer` |
| 6 | `GameOverScene`, `GameOverEvent` | `MenuScene`, `GameContext`, `KeyboardInput`, `SceneManager` |
| 7 | `WorldRenderer` | All entity `render()` methods, `RenderPipeline`, `GameContext`, `Camera` |
| 8 | Audio assets in `/resources/audio/` | `AudioCatalog`, `GameContext`, `PlayingScene`, `MenuScene` |
| 9 | `StrayDuckling` | `ScoreSystem`, `WorldLayout`, `GameContext` |

---

*Duckling Parade — Roadmap v1.0 — Brainless Game Jam 2026*