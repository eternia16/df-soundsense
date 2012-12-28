package cz.zweistein.df.soundsense;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.UIManager;

import org.xml.sax.SAXException;

import cz.zweistein.df.soundsense.config.ConfigurationXML;
import cz.zweistein.df.soundsense.config.SoundsXML;
import cz.zweistein.df.soundsense.glue.Glue;
import cz.zweistein.df.soundsense.gui.GameLogValidator;
import cz.zweistein.df.soundsense.gui.Gui;
import cz.zweistein.df.soundsense.input.file.LogReader;
import cz.zweistein.df.soundsense.output.sound.SoundProcesor;
import cz.zweistein.df.soundsense.util.log.LoggerSource;

public class SoundSense {
	private static Logger logger = LoggerSource.logger;
	
	public static void main(String[] args) {
		
		setLookAndFeel();
		
		try {
			
			ConfigurationXML configuration = new ConfigurationXML("configuration.xml");
			
			logger.info("SoundSense for Dwarf Fortress is starting...");
			logger.info(getVersionString());
			logger.info("Loading theme packs from "+configuration.getSoundpacksPath());
			SoundsXML soundsXML = new SoundsXML(configuration.getSoundpacksPath());
			logger.info("Done loading "+configuration.getSoundpacksPath()+", loaded "+soundsXML.getSounds().size()+" items.");
			
			new GameLogValidator(configuration).gamelogValidate();
			logger.info("Attempting to listen to "+configuration.getGamelogPath());
			LogReader logReader = new LogReader(configuration.getGamelogPath(), configuration.getGamelogEncoding());
			logger.info("Listening to "+configuration.getGamelogPath());
			
			SoundProcesor sp = new SoundProcesor(soundsXML, configuration.getPlaybackTheshhold());
			
			if (configuration.getGui()) {
				Gui.newGui(configuration, sp);
			}
			
			sp.setGlobalVolume(configuration.getVolume());
			
			Glue.glue(logReader, sp);
			
			//NetworkManager nm = new NetworkManager(sp);
			//nm.acceptIcommingConnections(3333);
			
		} catch (IOException e) {
			logger.severe("Exception :"+e+": "+e.toString());
		} catch (SAXException e) {
			logger.severe("Exception :"+e+": "+e.toString());
		}
		
	}

	private static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getVersionString() {
		Properties properties = new Properties();
		try {
			properties.load(SoundSense.class.getClassLoader().getResource("version.properties").openStream());
			return "release #" + properties.getProperty("release") + " build #" + properties.getProperty("buildNum") + " date " + properties.getProperty("buildDate");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}

}