# 🦆 Duckling Parade — Game Design Document

**Version:** 1.0 | **Date:** July 2026 | **Stage:** Pre-Alpha
**Jam:** Brainless Game Jam 2026

---

## 1. Overview
---

| Field | Value |
|---|---|
| **Game Title** | Duckling Parade |
| **Genre** | Arcade / Casual Top-Down |
| **Platform** | PC (Java 2D) |
| **Target Audience** | All ages — particularly families and casual players |
| **Tone** | Charming, wholesome, cute-stressful |
| **Scope** | Game Jam — small, completable in a single jam sprint |
| **Tech** | Java 2D (AWT/Swing), custom game loop (brainlessgamejam framework) |

### 1.1 Elevator Pitch

You are the mama duck. A trail of tiny ducklings follows close behind you. Guide your family safely across busy roads and past dangerous rivers — every duckling you save counts toward your score. Miss one, and you hear the splash. Don't let that happen.

### 1.2 Core Fantasy

The player should feel like a protective, waddling guardian. The ducklings are helpless and utterly trusting — they follow wherever mama leads. That trust is both the joy and the weight of the game.

---

## 2. Gameplay

### 2.1 Core Loop

1. Player controls the mama duck using WASD (top-down movement).
2. Ducklings trail behind in a conga-line formation, each occupying the previous position of the one ahead.
3. The play field consists of horizontal hazard zones to cross: roads, rivers, and safe open fields.
4. Surviving ducklings at the end of a crossing earn the player points. Lost ducklings are gone for that run.
5. Each wave/crossing becomes harder — more lanes, faster cars, wider rivers.
6. The run ends when mama duck is hit, or all ducklings are lost.

### 2.2 Controls

| Action | Key |
|---|---|
| Move Up | W / Arrow Up |
| Move Down | S / Arrow Down |
| Move Left | A / Arrow Left |
| Move Right | D / Arrow Right |
| Pause | Escape |
| Debug Overlay | F3 |

> Mama duck moves at a fixed speed. No sprint or acceleration curve — steady waddling feels more charming.

### 2.3 Duckling Follow System

Each duckling stores a queue of mama's recent positions. On each update tick, it moves to the position mama was N ticks ago (where N = its index in the line × a fixed delay constant). This creates the natural conga-line effect without complex pathfinding.

- Duckling 1 follows mama with delay D.
- Duckling 2 follows Duckling 1 with delay D.
- And so on — each link adds exactly one delay step.

Use a fixed-size `ArrayDeque<Vector2>` per Duckling. Each tick, pop the oldest position and offer mama's current position. The duckling renders at the oldest stored position.

> Delay constant D should be tuned to roughly half a duck-body-width per tick for tight, satisfying following.

### 2.4 Hazards

#### Roads

- Lanes of traffic moving horizontally across the screen.
- Cars move at fixed speeds — slow, medium, and fast lanes.
- Cars loop off-screen and re-enter from the other side.
- Contact with a car hits the **rearmost duckling** in the chain (not mama, unless it's a direct hit on mama).
- Contact with **mama duck** = instant game over.

#### Rivers

- Wide water strips the duck family must cross via lily pads or logs.
- Rivers are static zone hazards — stepping off a log or pad means a duckling falls in.
- Logs and lily pads move slowly — player must time movement.
- Mama duck can briefly enter shallow water edges; ducklings cannot.

#### Open Fields

- Safe zones between hazards — players catch their breath and regroup.
- May contain collectibles (bread crumbs) or bonus stray ducklings to rescue.

---

## 3. Scoring

### 3.1 Score Sources

| Event | Points |
|---|---|
| Each duckling alive at crossing completion | +100 pts |
| Perfect crossing (all ducklings intact) | +250 pts bonus |
| Rescued stray duckling | +150 pts |
| Speed bonus (finish under time threshold) | ×1.5 or ×2.0 multiplier |
| Wave survival multiplier | Wave number × 0.1 bonus |

### 3.2 Score Display

Score is displayed prominently in the HUD at all times. A small duckling silhouette counter shows how many ducklings remain in the current family. Wave number is displayed top-centre.

> High score is stored locally and displayed on the game over screen. No online leaderboard for jam scope.

---

## 4. Level Structure & Progression

### 4.1 Wave Structure

The game is structured as a series of crossings. Each crossing is one "wave". Waves increase in difficulty via the following parameters:

- Number of road lanes (starts at 2, increases to 4–5 by mid-game).
- Car speed (slow → fast, with eventual mixed speeds per lane).
- River width and log speed / spacing.
- Crossing time limit (optional, introduced after wave 5).

### 4.2 Difficulty Curve

| Waves | Description |
|---|---|
| 1–2 | Tutorial feel — 2 slow lanes, wide logs, forgiving timing. |
| 3–5 | Medium — 3 lanes, varying speeds, narrower log gaps. |
| 6–9 | Hard — 4 lanes, fast cars, rivers with moving logs and lily pads. |
| 10+ | Endless/brutal — maximum speed, tightest spacing. |

> `SPAWN_RATE_SCALE_PER_WAVE = 1.1f` from `GameLoopConfig` is a good reference scalar for car density per wave.

### 4.3 Level Themes *(Stretch Goal)*

- **Park path** — daytime, simple road.
- **Suburban street** — more lanes, some obstacles like bins.
- **Riverside crossing** — rivers with logs and pads.
- **City street** — max chaos, trams, cyclists, multiple rivers.

> For jam scope, a single tilesheet with road + grass + water is sufficient. Theme variety is a post-jam stretch.

---

## 5. Game States & Scenes

The existing framework models these as `Scene` implementations managed by `SceneManager`.

### 5.1 MenuScene

- Title screen with game logo, "Press Enter to Start", and high score display.
- Minimal animated background — a small duck walking across the screen would be charming.

### 5.2 PlayingScene

- Active gameplay — mama duck, ducklings, hazards, HUD.
- Delegates update to `GameSystem` and render to `RenderPipeline` (already wired).

### 5.3 PausedScene

- Semi-transparent overlay showing "PAUSED" and a resume prompt.
- Audio is paused on `enter()` and resumed on `exit()` — already implemented in `PausedScene`.

### 5.4 Game Over Screen

- Shows final score, ducklings saved, and wave reached.
- High score comparison and congratulatory/consoling message.
- "Play Again" and "Main Menu" options.

> `GameState.GAME_OVER` already exists in the enum. Implement as a new `Scene` rather than re-using `RenderPipeline`'s current stub.

---

## 6. Entities

### 6.1 MamaDuck (Player)

- Extends `Entity` — has position, velocity, bounds, active flag.
- Responds to keyboard input via `InputManager.movementDirection()`.
- **Collision:** car → game over | river tile → game over (unless on log) | log → ride.
- **Sprite:** top-down duck facing movement direction (4-directional sprites, 8 as a stretch).

### 6.2 Duckling

- One instance per follower in the line. Each stores a circular position history.
- **Collision:** car or river → `markInactive()`, publish `EntityDestroyed`.
- **Visual:** smaller duck sprite, slight colour variation (yellow → orange gradient down the chain).
- Brief splash or feather-puff particle on destruction *(stretch)*.

### 6.3 Car

- Moves horizontally at a fixed speed per lane. Wraps around screen edges.
- **Collision:** triggers `EntityDestroyed` on ducklings, game over on mama.
- **Sprites:** simple top-down car — 2–3 colour variants, left and right facing directions.

### 6.4 Log / Lily Pad

- Static or moving platform over river tiles.
- Mama and ducklings ride a log — they move with it while standing on it.
- If a duckling slides off the edge into water, it is destroyed.

### 6.5 Stray Duckling *(Bonus Entity)*

- Sitting in a safe zone. Mama walks into it to rescue — it joins the end of the line.
- Awards +150 pts on rescue.

### 6.6 Bread Crumb *(Collectible — Stretch)*

- Small collectible that awards a minor score bonus.

---

## 7. Audio

Audio infrastructure is complete (`JavaSoundAudioService`, `AudioCatalog`, `SoundType`). The following sounds need assets and registration in `AudioCatalog`:

### 7.1 Music

| SoundType | Description |
|---|---|
| `GAME_MENU_MUSIC` | Cheerful, bouncy loop — 8-bit or lo-fi chiptune. ~90 BPM. |
| `GAMEPLAY_MUSIC` | Upbeat but gentle. Loops seamlessly. |

### 7.2 Sound Effects

| SoundType | Description |
|---|---|
| `PLAYER_QUACK` | Mama duck quacks — on move input or periodically. |
| `DUCKLING_QUACK` | Higher pitched, shorter — plays when a duckling is rescued. |
| `DUCKLING_SPLASH` | Sad water splash — duckling lost to river. |
| `DUCKLING_HIT` | Soft thud/feather puff — duckling lost to car. |
| `CAR_PASS` | Whoosh — car passes close to mama without hitting. |
| `CROSSING_COMPLETE` | Happy chime / short fanfare — all ducklings crossed safely. |
| `GAME_OVER_STING` | Short sad horn or quack — death jingle. |
| `NAVIGATION_CLICK` | Already in `SoundType` — UI button hover click. |
| `NAVIGATION_CONFIRM` | Already in `SoundType` — UI confirm/select. |

> All SFX should be registered via `SoundDefinition.sfx()`. Music via `SoundDefinition.music()`. See `AudioCatalog`.

---

## 8. Visual Style

### 8.1 Art Direction

Top-down, 2D pixel art (or clean vector-style). Warm, soft palette. Ducks should read as round and fluffy even at small sizes. Roads are grey with white lane markings. Grass is bright green. Water is animated (wavy tile).

### 8.2 Suggested Sprite Sizes

| Element | Size |
|---|---|
| Mama Duck | 32×32 px |
| Duckling | 20×20 px |
| Car | 40×24 px (horizontal) / 24×40 px (vertical) |
| Log | 64×24 px |
| Lily Pad | 24×24 px |
| Tile (road / grass / water) | 32×32 px |

### 8.3 Camera

Camera follows mama duck. The existing `Camera` class supports `follow()`, `clamp()`, and camera shake — use camera shake on duckling loss for tactile feedback.

### 8.4 HUD Layout

| Position | Element |
|---|---|
| Top-left | Duckling silhouette count (e.g. 🦆🦆🦆🦆🦆) |
| Top-right | Current score |
| Top-centre | Wave number ("WAVE 3") |
| Bottom | Timer bar *(stretch, wave 5+)* |

---

## 9. Technical Notes & Framework Mapping

### 9.1 Entity Hierarchy

- `MamaDuck` and `Duckling` extend `Entity` — already `Collidable + Renderable + GameObject`.
- `Car` and `Log` extend `Entity` similarly.
- `CollisionSystem` automatically detects overlaps — subscribe to `CollisionEvent` in `PlayingScene.enter()`.

### 9.2 Event Flow (Collision)

```
CollisionSystem detects car ↔ duckling
  → publishes CollisionEvent
    → subscriber identifies duckling
    → calls markInactive()
    → publishes EntityDestroyed
      → score system adds points (subscriber)
      → audio plays SFX (subscriber)
```

### 9.3 Wave Management

- `WaveCompleted` event record already exists — publish when all hazards in a crossing are cleared.
- `WaveManager` subscribes: increments wave counter, queues next crossing parameters, plays fanfare SFX.

### 9.4 Useful Existing Hooks

| Class / Method | Use |
|---|---|
| `GameSystem.clear()` | Reset tick, elapsed time, state — call on restart. Already wired in `GameContext.restartRun()`. |
| `EventBus.clear()` | Drop stale subscriptions between runs. |
| `Camera.shake()` | Trigger on duckling loss for tactile feedback. |
| `Timer` | Countdown timer per crossing. Note: initialise `start` field on reset (not set in constructor currently). |
| `GameLoopConfig.SPAWN_RATE_SCALE_PER_WAVE` | Use as car density scalar per wave. |
| `GameLoopConfig.BETWEEN_WAVE_DELAY_MS` | 3000 ms gap between crossings — already defined. |

---

## 10. Scope Definition

### 10.1 In Scope (Jam Build)

- Mama duck + duckling follow chain.
- Road hazard with at least 3 car speeds.
- River hazard with logs.
- 5–10 waves of increasing difficulty.
- Score system — per-duckling, per-crossing, local high score.
- Menu, Playing, Paused, Game Over scenes.
- Basic SFX and music (if assets available).
- Debug overlay (F3) — already implemented.

### 10.2 Stretch Goals

- Stray duckling rescue mechanic.
- Bread crumb collectibles.
- Camera shake on loss events.
- Particle effects (splashes, feathers).
- 4-directional animation for duck sprites.
- Multiple visual themes (park → city).
- Wave transition screen with duckling count summary.
- Persistent high score to disk.

### 10.3 Explicitly Out of Scope

- Multiplayer.
- Story or dialogue.
- Procedural generation.
- Mobile / controller support.
- Online leaderboard.

---

## 11. Suggested Development Milestones

| # | Milestone | Done When... |
|---|---|---|
| 1 | **Move & Follow** | Mama duck moves with WASD. One duckling follows in a chain. Bounds visible in debug. |
| 2 | **Hazards** | Cars move across lanes. Collision with duckling marks it inactive and logs to console. |
| 3 | **River** | River tiles block movement. Logs spawn and move. Riding logic works. |
| 4 | **Scoring & HUD** | Score increments on crossing completion. Duckling count displayed. Wave counter advances. |
| 5 | **Scenes** | Menu and Game Over scenes functional. Restart wired. Pause works. |
| 6 | **Audio** | Music plays in menu and gameplay. SFX on duckling loss and crossing complete. |
| 7 | **Polish** | Sprites replace placeholder rects. Camera follow enabled. Difficulty curve tuned. |
| 8 | **Jam Submission** | Build tested, packaged as runnable JAR, GDD and controls noted in README. |

---

*Duckling Parade — GDD v1.0 — Brainless Game Jam 2026*