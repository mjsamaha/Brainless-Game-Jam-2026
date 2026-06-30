package com.lobsterchops.brainlessgamejam.event;

/**
 * Published when the last enemy in a wave is destroyed and the wave is cleared.
 *
 * <p>Typical publishers: {@code WaveManager} (Phase 5).</p>
 * <p>Typical subscribers: wave manager itself (to advance wave counter),
 * score system (wave clear bonus), audio service (fanfare SFX).</p>
 *
 * @param waveNumber the wave that just completed (1-indexed)
 */
public record WaveCompleted(int waveNumber) {
}
 