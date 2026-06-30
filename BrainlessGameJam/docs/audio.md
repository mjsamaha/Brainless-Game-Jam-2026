# Audio

How sounds get registered, played, and mixed.

## The pieces

```
AudioService (interface)          — what gameplay code calls
JavaSoundAudioService (impl)      — javax.sound.sampled-based implementation
AudioCatalog                      — static registry: SoundType -> SoundDefinition
SoundDefinition (record)          — path, category, looping, base volume, max instances
SoundType (enum)                  — every sound the game knows about, by name
AudioCategory (enum)              — SFX or MUSIC
AudioMath                         — linear (0-1) <-> decibel conversion helpers
```

`AudioService` is resolved via `ServiceLocator.resolve(AudioService.class)` — `GameContext` constructs `JavaSoundAudioService` and calls `init()` on it during bootstrap, so it's ready to use from any system that has access to the locator.

## Registering a new sound

Every sound needs a `SoundType` entry and a matching `SoundDefinition` in `AudioCatalog`.

1. Add the enum value in `SoundType.java`:
   ```java
   public enum SoundType {
       NAVIGATION_CLICK, NAVIGATION_CONFIRM,
       GAME_MENU_MUSIC, GAMEPLAY_MUSIC,
       PLAYER_JUMP   // new
   }
   ```

2. Register it in `AudioCatalog`'s static block:
   ```java
   register(defs, SoundType.PLAYER_JUMP,
       SoundDefinition.sfx("/audio/jump.wav", 0.8f, 3));
   ```

   `SoundDefinition.sfx(path, baseVolume, maxInstances)` builds a non-looping SFX definition. `SoundDefinition.music(path, baseVolume)` builds a looping music definition with `maxInstances = 1`. The resource path must start with `/` and resolve on the classpath (same convention as `ResourceLoader`/`FontLoader`), e.g. a file at `src/main/resources/audio/jump.wav`.

3. The audio file itself needs to actually exist on the classpath at that path, or `JavaSoundAudioService.createClip()` will log a warning and silently no-op (it returns `null`, callers handle that gracefully rather than throwing).

`AudioCatalog` has a `STRICT_VALIDATION` flag (currently `false`) that, if flipped to `true`, throws at startup if any `SoundType` value lacks a registered `SoundDefinition`. Useful once you're closer to done and want to catch "forgot to register a sound" mistakes immediately rather than discovering them as silent no-ops at playback time.

## Playing sounds

```java
AudioService audio = ServiceLocator.resolve(AudioService.class);

audio.playSfx(SoundType.PLAYER_JUMP);
audio.playMusic(SoundType.GAMEPLAY_MUSIC);              // won't restart if already playing
audio.playMusic(SoundType.GAMEPLAY_MUSIC, true);        // force restart from frame 0
audio.stopMusic();
```

SFX vs music aren't interchangeable — calling `playSfx()` on a `SoundType` registered as `MUSIC` (or vice versa) logs a warning and does nothing; the category check in `JavaSoundAudioService` enforces this.

SFX supports overlapping instances up to `maxInstances` (defined per-sound in its `SoundDefinition`) — past that, the oldest active clip for that `SoundType` is stopped and closed to make room (`enforceMaxInstances`). Music only ever has one active clip at a time; starting new music stops whatever's currently playing first.

## Pause/resume

```java
audio.pauseAll();
audio.resumeAll();
```

These are already wired into `GameUpdater.togglePause()` — pausing the game pauses all active clips (music + SFX), resuming restarts them from where Java Sound left off (clips are stopped, not closed, so position is preserved). You generally won't need to call these directly unless you're building a pause path outside the existing `TOGGLE_PAUSE` command.

## Volume

Three independent 0.0–1.0 sliders, all clamped via `AudioMath.clamp01`:

```java
audio.setMasterVolume(0.8f);
audio.setMusicVolume(0.6f);
audio.setSfxVolume(1.0f);
```

Effective volume for any given clip is `masterVolume * (musicVolume or sfxVolume) * def.baseVolume()` — the per-sound `baseVolume` from its `SoundDefinition` is a fixed multiplier baked in at registration time (useful for sounds that are inherently louder/quieter than others without needing a separate slider). `AudioMath.linearToDb` converts the final linear value to decibels for `FloatControl.MASTER_GAIN`, clamped to whatever range the underlying `Clip`'s control actually supports.

Changing any volume setter immediately re-applies gain to all currently active clips (`applyVolumesToActiveClips()`), not just future ones.

## Per-frame update

`audioService.update()` is already called every tick from `GameUpdater.update()` — it cleans up clips that have finished playing (SFX naturally, non-looping music when it ends) so `activeSfx`/`currentMusicClip` don't accumulate stale references. You don't need to call this yourself.