package com.lobsterchops.brainlessgamejam.audio;


public interface AudioService {
	
	void init();
	
	void shutdown();
	
	void update();
	
	void playSfx(SoundType type);
	
	void playMusic(SoundType type);
	
	void playMusic(SoundType type, boolean restartIfSameTrack);
	
	void stopMusic();
	
	void pauseAll();
	
	void resumeAll();
	
	void setMasterVolume(float volume);
	
	void setMusicVolume(float volume);
	
	void setSfxVolume(float volume);
	
	float getMasterVolume();
	
	float getMusicVolume();
	
	float getSfxVolume();
	
}
