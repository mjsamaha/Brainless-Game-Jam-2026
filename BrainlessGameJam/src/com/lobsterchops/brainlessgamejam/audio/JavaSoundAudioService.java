package com.lobsterchops.brainlessgamejam.audio;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class JavaSoundAudioService implements AudioService {

	private static final Logger LOGGER = Logger.getLogger(JavaSoundAudioService.class.getName());

	private final Map<AudioType, SoundDefinition> catalog;
	private final Map<AudioType, Deque<Clip>> activeSfx = new EnumMap<>(AudioType.class);
	private final Set<Clip> pausedClips = Collections.newSetFromMap(new IdentityHashMap<Clip, Boolean>());
	private final Map<String, Deque<Clip>> clipPool = new HashMap<>();

	private Clip currentMusicClip;
	private AudioType currentMusicType;

	private float masterVolume = 1.0f;
	private float musicVolume = 1.0f;
	private float sfxVolume = 1.0f;

	private boolean initialized;
	private boolean paused;

	public JavaSoundAudioService() {
		this(AudioCatalog.definitions());
	}

	public JavaSoundAudioService(Map<AudioType, SoundDefinition> catalog) {
		Objects.requireNonNull(catalog, "catalog must not be null");
		EnumMap<AudioType, SoundDefinition> copied = new EnumMap<>(AudioType.class);
		copied.putAll(catalog);
		this.catalog = copied;
	}

	@Override
	public synchronized void init() {
		if (initialized)
			return;
		initialized = true;
		paused = false;
		preloadSfx();
	}

	private void preloadSfx() {
		for (Map.Entry<AudioType, SoundDefinition> entry : catalog.entrySet()) {
			SoundDefinition def = entry.getValue();
			if (def.category() != AudioCategory.SFX)
				continue;
			Deque<Clip> pool = new ArrayDeque<>();
			for (int i = 0; i < def.maxInstances(); i++) {
				Clip clip = createClip(def.resourcePath());
				if (clip != null)
					pool.addLast(clip);
			}
			clipPool.put(def.resourcePath(), pool);
		}
	}

	@Override
	public synchronized void shutdown() {
		stopMusic();

		for (Deque<Clip> clips : activeSfx.values()) {
			for (Clip clip : clips) {
				closeClip(clip);
			}
		}
		activeSfx.clear();
		pausedClips.clear();
		paused = false;
		initialized = false;
	}

	@Override
	public synchronized void update() {
		if (!initialized || paused)
			return;
		cleanupFinishedSfx();
		cleanupFinishedMusicIfNeeded();
	}

	@Override
	public synchronized void playSfx(AudioType type) {
		ensureInitialized();

		SoundDefinition def = findDef(type);
		if (def == null)
			return;
		if (def.category() != AudioCategory.SFX) {
			LOGGER.warning(() -> "playSfx called with non-SFX type: " + type);
			return;
		}
		if (paused)
			return;

		Deque<Clip> pool = clipPool.get(def.resourcePath());
		if (pool == null || pool.isEmpty())
			return; // pool exhausted or not loaded

		Clip clip = pool.pollFirst();
		applyClipGain(clip, masterVolume * sfxVolume * def.baseVolume());
		startClip(clip, false);

		// Track it so we can return it to the pool when done
		activeSfx.computeIfAbsent(type, id -> new ArrayDeque<>()).addLast(clip);
	}

	@Override
	public synchronized void playMusic(AudioType type) {
		playMusic(type, false);
	}

	@Override
	public synchronized void playMusic(AudioType type, boolean restartIfSameTrack) {
		ensureInitialized();

		SoundDefinition def = findDef(type);
		if (def == null)
			return;
		if (def.category() != AudioCategory.MUSIC) {
			LOGGER.warning(() -> "playMusic called with non-MUSIC type: " + type);
			return;
		}

		if (currentMusicType == type && currentMusicClip != null && currentMusicClip.isOpen()) {
			if (restartIfSameTrack) {
				startClip(currentMusicClip, def.looping());
			}
			return;
		}

		stopMusic();

		Clip newMusic = createClip(def.resourcePath());
		if (newMusic == null)
			return;

		currentMusicClip = newMusic;
		currentMusicType = type;

		applyClipGain(currentMusicClip, masterVolume * musicVolume * def.baseVolume());

		if (!paused) {
			startClip(currentMusicClip, def.looping());
		} else {
			pausedClips.add(currentMusicClip);
		}
	}

	@Override
	public synchronized void stopMusic() {
		if (currentMusicClip != null) {
			closeClip(currentMusicClip);
			currentMusicClip = null;
			currentMusicType = null;
		}
	}

	@Override
	public synchronized void pauseAll() {
		if (!initialized || paused)
			return;

		paused = true;

		pauseClip(currentMusicClip);

		for (Deque<Clip> clips : activeSfx.values()) {
			for (Clip clip : clips) {
				pauseClip(clip);
			}
		}
	}

	@Override
	public synchronized void resumeAll() {
		if (!initialized || !paused)
			return;

		if (currentMusicClip != null && pausedClips.contains(currentMusicClip) && currentMusicClip.isOpen()) {
			SoundDefinition musicDef = findDef(currentMusicType);
			if (musicDef != null) {
				startClip(currentMusicClip, musicDef.looping());
			}
			pausedClips.remove(currentMusicClip);
		}

		for (Map.Entry<AudioType, Deque<Clip>> entry : activeSfx.entrySet()) {
			SoundDefinition def = findDef(entry.getKey());
			if (def == null)
				continue;
			for (Clip clip : entry.getValue()) {
				if (pausedClips.contains(clip) && clip.isOpen()) {
					startClip(clip, def.looping());
					pausedClips.remove(clip);
				}
			}
		}

		paused = false;
	}

	@Override
	public synchronized void setMasterVolume(float volume) {
		masterVolume = AudioMath.clamp01(volume);
		applyVolumesToActiveClips();
	}

	@Override
	public synchronized void setMusicVolume(float volume) {
		musicVolume = AudioMath.clamp01(volume);
		applyVolumesToActiveClips();
	}

	@Override
	public synchronized void setSfxVolume(float volume) {
		sfxVolume = AudioMath.clamp01(volume);
		applyVolumesToActiveClips();
	}

	@Override
	public synchronized float getMasterVolume() {
		return masterVolume;
	}

	@Override
	public synchronized float getMusicVolume() {
		return musicVolume;
	}

	@Override
	public synchronized float getSfxVolume() {
		return sfxVolume;
	}

	private SoundDefinition findDef(AudioType type) {
		if (type == null) {
			LOGGER.warning("type must not be null");
			return null;
		}
		SoundDefinition def = catalog.get(type);
		if (def == null) {
			LOGGER.fine(() -> "No SoundDefinition registered for " + type);
			return null;
		}
		return def;
	}

	private void ensureInitialized() {
		if (!initialized) {
			throw new IllegalStateException("AudioService not initialized. Call init() first.");
		}
	}

	private void enforceMaxInstances(Deque<Clip> clips, int maxInstances) {
		while (clips.size() >= maxInstances) {
			Clip oldest = clips.pollFirst();
			closeClip(oldest);
		}
	}

	private void applyVolumesToActiveClips() {
		if (currentMusicClip != null && currentMusicType != null) {
			SoundDefinition def = findDef(currentMusicType);
			if (def != null) {
				applyClipGain(currentMusicClip, masterVolume * musicVolume * def.baseVolume());
			}
		}

		for (Map.Entry<AudioType, Deque<Clip>> entry : activeSfx.entrySet()) {
			SoundDefinition def = findDef(entry.getKey());
			if (def == null)
				continue;
			float effective = masterVolume * sfxVolume * def.baseVolume();

			for (Clip clip : entry.getValue()) {
				applyClipGain(clip, effective);
			}
		}
	}

	private void applyClipGain(Clip clip, float linearVolume) {
		if (clip == null || !clip.isOpen())
			return;
		if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
			return;

		FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float db = AudioMath.toControlDb(linearVolume, gain);
		gain.setValue(db);
	}

	private void cleanupFinishedSfx() {
		Iterator<Map.Entry<AudioType, Deque<Clip>>> mapIt = activeSfx.entrySet().iterator();

		while (mapIt.hasNext()) {
			Map.Entry<AudioType, Deque<Clip>> entry = mapIt.next();
			SoundDefinition def = findDef(entry.getKey());
			Deque<Clip> clips = entry.getValue();

			Iterator<Clip> clipIt = clips.iterator();
			while (clipIt.hasNext()) {
				Clip clip = clipIt.next();
				if (shouldDisposeClip(clip)) {
					clipIt.remove();
					// Return to pool instead of closing
					if (def != null) {
						Deque<Clip> pool = clipPool.get(def.resourcePath());
						if (pool != null) {
							clip.stop();
							clip.setFramePosition(0);
							pool.addLast(clip);
							continue;
						}
					}
					closeClip(clip); // fallback if pool not found
				}
			}

			if (clips.isEmpty())
				mapIt.remove();
		}
	}

	private void cleanupFinishedMusicIfNeeded() {
		if (currentMusicClip == null || currentMusicType == null)
			return;
		if (pausedClips.contains(currentMusicClip))
			return;

		SoundDefinition def = findDef(currentMusicType);
		if (def == null)
			return;
		if (def.looping())
			return;

		if (!currentMusicClip.isRunning() && !currentMusicClip.isActive()) {
			closeClip(currentMusicClip);
			currentMusicClip = null;
			currentMusicType = null;
		}
	}

	private boolean shouldDisposeClip(Clip clip) {
		if (clip == null)
			return true;
		if (!clip.isOpen())
			return true;
		if (pausedClips.contains(clip))
			return false;
		return !clip.isRunning() && !clip.isActive();
	}

	private void pauseClip(Clip clip) {
		if (clip == null || !clip.isOpen())
			return;
		if (clip.isRunning()) {
			clip.stop();
			pausedClips.add(clip);
		}
	}

	private void startClip(Clip clip, boolean loop) {
		if (clip == null || !clip.isOpen())
			return;

		clip.stop();
		clip.setFramePosition(0);

		if (loop) {
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		} else {
			clip.start();
		}
	}

	private Clip createClip(String resourcePath) {
		URL url = getClass().getResource(resourcePath);
		if (url == null) {
			LOGGER.warning("Audio resource not found: " + resourcePath);
			return null;
		}

		try (AudioInputStream stream = AudioSystem.getAudioInputStream(url)) {
			Clip clip = AudioSystem.getClip();
			clip.open(stream);
			return clip;
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			LOGGER.log(Level.WARNING, "Failed to load audio resource: " + resourcePath, e);
			return null;
		}
	}

	private void closeClip(Clip clip) {
		if (clip == null)
			return;

		pausedClips.remove(clip);

		try {
			if (clip.isRunning()) {
				clip.stop();
			}
		} catch (Exception e) {
			LOGGER.log(Level.FINE, "Error stopping clip", e);
		}

		try {
			if (clip.isOpen()) {
				clip.close();
			}
		} catch (Exception e) {
			LOGGER.log(Level.FINE, "Error closing clip", e);
		}
	}
}