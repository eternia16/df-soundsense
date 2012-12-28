package cz.zweistein.df.soundsense.output.sound.player;

import java.util.logging.Logger;

import cz.zweistein.df.soundsense.config.Sound;
import cz.zweistein.df.soundsense.util.log.LoggerSource;

public class ChannelThread implements Runnable {
	private static Logger logger = LoggerSource.logger;
	
	private SPIPlayer musicPlayer;
	private Sound loopMusic;
	private String singualMusic;
	private String currentMusic;
	private float defaultGain = 0;
	private boolean mute;
	
	private String channelName;
	
	private PlayerManager manager;

	public void setDefaultGain(float defaultGain) {
		logger.finest("Setting "+defaultGain+" gain for "+channelName);
		this.defaultGain = defaultGain;
		if (musicPlayer != null) {
			musicPlayer.setMasterGain(defaultGain);
		}
	}

	public boolean isMute() {
		return this.mute;
	}

	public void setMute(boolean mute) {
		logger.finest("Muting "+mute+" for "+channelName);
		if (musicPlayer != null) {
			musicPlayer.setMute(mute);
		}
		this.mute = mute;
	}

	public ChannelThread(String channelName, PlayerManager manager) {
		this.channelName = channelName;
		this.manager = manager;
	}

	public void setLoopMusic(Sound sound) {
		this.loopMusic = sound;
	}
	
	public Sound getLoopMusic() {
		return this.loopMusic;
	}

	public void setCurrentMusic(String sound) {
		this.currentMusic = sound;
	}
	
	public String getCurrentMusic() {
		return this.currentMusic;
	}

	public SPIPlayer getMusicPlayer() {
		return this.musicPlayer;
	}

	public String getChannelName() {
		return this.channelName;
	}

	@Override
	public void run() {
		
		while(true) {
			
			try {
				
				if (singualMusic != null || loopMusic != null) {
			
					currentMusic = singualMusic;
					singualMusic = null;
					
					if (currentMusic == null && loopMusic != null) {
						currentMusic = loopMusic.getRandomSoundFile().getFileName();
						logger.fine("Channel looping "+channelName+": "+currentMusic);
					}

					logger.finest("Channel playing "+currentMusic);
		
					musicPlayer = new SPIPlayer();
					
					logger.fine(channelName+": "+currentMusic);
					musicPlayer.setMasterGain(this.defaultGain);
					musicPlayer.setMute(this.mute);
					
					this.manager.channelStatusChanged(this);
					musicPlayer.play(currentMusic);
				
				} else {
					Thread.sleep(100);
				}
			
			} catch (Exception e) {
				logger.severe("Exception :"+e+": "+e.toString());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
			
			
		} 
		
	}

	@Override
	public String toString() {
		return this.channelName+((this.currentMusic==null)?"":": "+this.currentMusic);
	}

	public void setSingualMusic(String singualMusic) {
		this.singualMusic = singualMusic;
	}

}