package cz.zweistein.df.soundsense.output.sound;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.zweistein.df.soundsense.config.ConfigurationXML;
import cz.zweistein.df.soundsense.config.sounds.Sound;
import cz.zweistein.df.soundsense.config.sounds.SoundsXML;
import cz.zweistein.df.soundsense.output.Procesor;
import cz.zweistein.df.soundsense.output.sound.player.PlayerManager;
import cz.zweistein.df.soundsense.util.log.LoggerSource;

public class SoundProcesor extends Procesor {
	private static Logger logger = LoggerSource.LOGGER;

	private SoundsXML soundsXML;

	private ConfigurationXML configuration;

	private PlayerManager player;

	private Pattern coordinatePattern;

	public SoundProcesor(SoundsXML soundsXML, ConfigurationXML configurationXML) {
		this.soundsXML = soundsXML;
		this.configuration = configurationXML;
		this.player = new PlayerManager(configurationXML);
		this.player.setPlaybackTheshhold(configurationXML.getPlaybackTheshhold());

		this.coordinatePattern = Pattern.compile("\\[(\\-?\\d),(\\-?\\d),(\\-?\\d)\\] (.*)");
	}

	public SoundsXML getSoundsXML() {
		return this.soundsXML;
	}

	public void setGlobalVolume(float globalVolume) {
		this.player.setGlobalVolume(globalVolume);
	}

	@Override
	public void processLine(String nextLine) {

		int matches = 0;
		Sound matchedSound = null;

		// parse location "[x,y,z] eventtext"
		Matcher matcher = coordinatePattern.matcher(nextLine);

		Long x = null;
		Long y = null;
		Long z = null;

		if (matcher.find()) {
			x = Long.parseLong(matcher.group(1));
			y = Long.parseLong(matcher.group(2));
			z = Long.parseLong(matcher.group(3));
			nextLine = matcher.group(4);
		}

		for (Sound sound : soundsXML.getSounds()) {

			if (!getConfiguration().getDisabledSounds().contains(sound.getParentFile()) && sound.matches(nextLine)) {
				matches++;
				matchedSound = sound;

				logger.fine("Message '" + nextLine + "' matched event '" + sound.toString() + "' from '" + sound.getParentFile() + "'.");

				player.playSound(sound, x, y, z);

				if (sound.getHaltOnMatch()) {
					logger.finest("Ending matching prematurely as expected.");
					break;
				} else {
					logger.fine("Continuing for next rule match.");
				}
			}
		}
		if (matchedSound == null) {
			logger.info(nextLine);
			logger.fine("Message '" + nextLine + "' did not match any rule.");
		} else {
			StringBuilder sb = new StringBuilder();

			if (matchedSound.getAnsiFormat() != null) {
				sb.append(matchedSound.getAnsiFormat());
			}
			sb.append(nextLine);

			logger.info(sb.toString());
			logger.finest("Message '" + nextLine + "' matched " + matches + " rules.");
		}
	}

	public PlayerManager getPlayerManager() {
		return this.player;
	}

	public void setPlaybackTheshhold(long playbackTheshhold) {
		this.player.setPlaybackTheshhold(playbackTheshhold);
	}

	public ConfigurationXML getConfiguration() {
		return configuration;
	}

}
