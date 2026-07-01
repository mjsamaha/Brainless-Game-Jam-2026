# Game Design Document
## Brainless Game Jam 2026
### Theme: "Little Guys, or Large Groups of Small Fellows"

---

## 1. Concept

**Working Title:** TBD

**Logline:** You are something large and frightening. A swarm of tiny pixel creatures shares your arena. Most of them are innocent — don't touch them. Some are imposters — hunt them down. The longer you stay clean, the more your score multiplier grows. The more you collect, the bigger you become. Manage your trail or be crushed by your own success.

**Genre:** Single-screen arcade / swarm avoidance

**Platform:** PC (Java / Swing)

**Jam:** Brainless Game Jam 2026

---

## 2. Core Loop

```
Wave starts
    → Swarm spawns (friendlies + imposters, randomised)
    → Player moves through arena
    → Multiplier builds over time
    → Player hunts imposters (collision = tag = trail segment removed + points × multiplier)
    → Player avoids friendlies (collision = trail segment added + multiplier reset + points lost)
    → Wave ends when timer expires or all imposters tagged
    → Score tallied, trail carries over to next wave
    → Next wave spawns with larger swarm, wilder imposter behavior
Repeat
```

---

## 3. Arena

- Fixed square walled arena, centred on screen
- Walls are solid — entities and player bounce or steer off them
- No scrolling, no camera movement — everything visible at all times
- Arena size is constant across all waves (the growing trail is the difficulty scaling, not the arena shrinking)

---

## 4. Entities

### 4.1 Friendlies
- **Colour:** Blue / green (exact shade TBD in ColorConfig)
- **Behaviour:** Wander randomly, flee the player when within a proximity radius
- **On collision with player:** Join the player's trail at the back. Score penalty applied. Multiplier resets.
- **Count per wave:** Scales up each wave

### 4.2 Imposters
- **Colour:** Red
- **Behaviour:** Same base wander/flee as friendlies, but with per-entity randomised modifiers:
  - Speed: randomly faster or slower than baseline
  - Flee pattern: one of — standard (mirror friendly), erratic (jittery direction changes), delayed (slow to react), overshoot (flee past the safe point)
- **On collision with player:** Trail segment is added briefly, then detaches and disappears (net trail shrink by one). Points added, multiplied by current multiplier.
- **Count per wave:** Scales up each wave, ratio vs friendlies shifts toward more imposters in later waves

### 4.3 Player
- **Shape:** Single entity, visually distinct (larger than swarm entities)
- **Movement:** WASD, 8-directional, fixed speed
- **Trail:** Ordered list of previously occupied positions; collected entities snap to this path snake-style
- **Effective size:** Grows as trail lengthens — the bounding area of player + trail is the real collision surface late game
- **Trail persists across waves** — you carry what you collected into the next wave

---

## 5. Scoring

### 5.1 Multiplier
- Starts at **1.0x** at the beginning of each wave
- Increases by **+0.1x per second** passively while the wave is active
- **Resets to 1.0x** immediately on any friendly collision
- Is **not reset** by imposter collisions (rewarding clean imposter tags at high multiplier)

### 5.2 Points
| Event | Effect |
|---|---|
| Wave starts | Player given base score (e.g. 1000 pts) |
| Imposter tagged | **+points × current multiplier** |
| Friendly collected | **−fixed penalty** (e.g. −150 pts) + multiplier reset |
| Wave completed (all imposters) | **Bonus** for time remaining |
| Wave completed (timer expired) | Score as-is, no bonus |

- Score can go negative
- Score carries across waves (running total)

### 5.3 Trail Economy Summary
| Collision type | Trail | Score | Multiplier |
|---|---|---|---|
| Friendly | +1 segment (permanent until imposter tag) | −penalty | Reset to 1x |
| Imposter | +1 segment → immediately removed | +points × multiplier | Unchanged |

---

## 6. Wave Structure

| Property | Wave 1 | Scaling |
|---|---|---|
| Friendly count | ~8 | +3–4 per wave |
| Imposter count | ~3 | +2 per wave |
| Wave timer | 45s | −2s per wave (floor ~20s) |
| Imposter speed variance | ±10% | ±5% more per wave |
| Flee radius (player detection) | Medium | Slight increase per wave |

Wave ends on whichever comes first:
- Timer expires
- All imposters tagged

---

## 7. Game States

Mapped to existing `GameState` enum (will require additions):

| State | Description |
|---|---|
| `MENU` | Title screen, start prompt |
| `PLAYING` | Active wave in progress |
| `WAVE_COMPLETE` | Brief results screen between waves |
| `PAUSED` | ESC pause, audio paused |
| `GAME_OVER` | Running score went deeply negative, or manual condition TBD |

---

## 8. Controls

| Input | Action |
|---|---|
| W / A / S / D | Move player |
| ESC | Pause / unpause |
| F3 | Toggle debug overlay |

Menu navigation bindings to be added to `InputAction` and `KeyboardInput`.

---

## 9. New Systems Required

Listed in proposed build order:

### Phase A — Arena & Player
- `Arena.java` — defines wall bounds, renders border, handles entity bounce
- `PlayerEntity.java` — extends `Entity`, WASD movement, trail management
- `TrailSystem.java` — records player path history, positions trail segments, handles attach/detach logic

### Phase B — Swarm Entities
- `FriendlyEntity.java` — extends `Entity`, wander + flee AI
- `ImposterEntity.java` — extends `Entity`, wander + flee AI + randomised modifiers
- `SwarmAI.java` — shared flee/wander behaviour, takes player position as input

### Phase C — Wave System
- `WaveConfig.java` — data class defining entity counts, timer, variance per wave
- `WaveManager.java` — spawns entities per config, owns wave timer, fires wave-end events
- `WaveState.java` (or extend `GameState`) — tracks current wave number

### Phase D — Scoring
- `ScoreTracker.java` — owns running score, multiplier value, exposes update methods
- `MultiplierTimer.java` — ticks multiplier up per second, resets on friendly hit
- Hooks into existing `EventBus` — listens for `CollisionEvent`, routes to score logic

### Phase E — UI / Feedback
- HUD: current score, multiplier, wave timer, wave number displayed on screen
- Visual feedback on collision (flash entity, brief screen effect)
- Wave complete / game over screens
- Menu screen

---

## 10. Architecture Notes

These are grounded in the existing codebase:

- **Entity base class** — `FriendlyEntity` and `ImposterEntity` both extend `Entity` from `entity/`. Collision via `Collidable` / `getBounds()` already in place.
- **Collision events** — `CollisionSystem` already publishes `CollisionEvent` via `EventBus`. Score and trail logic subscribe to these events rather than polling.
- **Wave spawning** — slots into Phase 5 of the planned build order (Wave/Spawner system). `WaveManager` calls `GameSystem.addObject()`.
- **GameState** — `WAVE_COMPLETE` needs to be added to the enum. Pause-as-scene-transition pattern already established.
- **Trail** — implemented as an ordered `Deque<Vector2>` of player position history. Trail segment entities read their position from this queue by index.
- **ServiceLocator** — `ScoreTracker` and `WaveManager` registered here for cross-system access.
- **ColorConfig** — friendly and imposter colours added as named constants here.
- **RenderLayer** — trail segments render on `ENTITIES` layer, HUD on a new `HUD` layer added to the enum.

---

## 11. Known Deferred Items (pre-game work)

These gaps exist in the boilerplate and will need resolving before or during game implementation:

- `flushPendingObjects()` is an empty stub — needs implementing before entities can be spawned mid-wave
- `Camera` shake wiring deferred — could be used for collision feedback
- `Timer.java` has no setter — needs `start(long duration)` method before use in wave timing
- Menu key bindings missing from `KeyboardInput`
- No audio assets registered yet in `AudioCatalog`

---

## 12. Open Questions

- **Game title** — needs a name
- **Player visual** — what does the player entity look like? A boot? A giant pixel blob? A cursor?
- **Lose condition precision** — is there a hard score floor that triggers GAME_OVER, or does the game always run for N waves regardless?
- **Trail visual** — do trail segments retain their colour (red/green) so you can see your guilt at a glance, or do they all become one neutral colour?
- **Sound design** — what does a clean imposter tag sound like vs a friendly collision? (satisfying vs punishing)

---

*Document version: 0.1 — pre-implementation*
*Last updated: Brainless Game Jam 2026*