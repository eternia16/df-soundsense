package cz.zweistein.df.soundsense.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.zweistein.df.soundsense.gui.control.Threshold;
import cz.zweistein.df.soundsense.util.log.LoggerSource;

public class ConfigurationXML extends XMLConfig {
	private static Logger logger = LoggerSource.LOGGER;

	private String fileName;
	private Document doc;

	private String gamelogPath;
	private String gamelogEncoding;
	private String soundpacksPath;
	private boolean allowPackOverride;
	private boolean noWarnAbsolutePaths;
	private float volume;
	private long playbackTheshhold;

	private List<String> autoUpdateURLs;
	private boolean replaceFiles;
	private boolean deleteFiles;

	private boolean gui;

	private Set<String> disabledSounds;
	private Set<String> mutedChannels;

	private List<String> supplementalLogs;

	private boolean achievements;
	
	private long expectedMinimalSoundsCount;

	public ConfigurationXML(String configurationFile) throws SAXException, IOException {

		this.fileName = configurationFile;

		this.doc = parseDoc(fileName);

		Element configNodes = (Element) doc.getElementsByTagName("configuration").item(0);

		Node gamelogNode = configNodes.getElementsByTagName("gamelog").item(0);
		this.gamelogPath = gamelogNode.getAttributes().getNamedItem("path").getNodeValue();
		this.gamelogEncoding = gamelogNode.getAttributes().getNamedItem("encoding").getNodeValue();

		Node soundpacksNode = configNodes.getElementsByTagName("soundpacks").item(0);
		this.soundpacksPath = soundpacksNode.getAttributes().getNamedItem("defaultPath").getNodeValue();
		this.allowPackOverride = parseBooleanAttribute(soundpacksNode, "allowOverride", true);
		this.noWarnAbsolutePaths = parseBooleanAttribute(soundpacksNode, "noWarnAbsolutePath", true);

		Node volumeNode = configNodes.getElementsByTagName("volume").item(0);
		this.volume = parseFloatAttribute(volumeNode, "value", this.volume);

		Node playbackTheshholdNode = configNodes.getElementsByTagName("playbackTheshhold").item(0);
		this.playbackTheshhold = parseLongAtribute(playbackTheshholdNode, "value", Threshold.EVERYTHING.getValue());

		Node autoUpdateURLNode = configNodes.getElementsByTagName("autoUpdateURLs").item(0);
		this.autoUpdateURLs = parsePathList(autoUpdateURLNode);

		this.setDeleteFiles(parseBooleanTag("autoUpdateDeleteFiles", configNodes));
		this.setReplaceFiles(parseBooleanTag("autoUpdateReplaceFiles", configNodes));

		this.gui = parseBooleanTag("gui", configNodes);

		this.achievements = parseBooleanTag("achievements", configNodes);
		
		Node expectedMinimalSoundsCountNode = configNodes.getElementsByTagName("expectedMinimalSoundsCount").item(0);
		this.expectedMinimalSoundsCount = parseLongAtribute(expectedMinimalSoundsCountNode, "value", 100l);

		Node disabledSoundsNode = configNodes.getElementsByTagName("disabledSounds").item(0);
		this.disabledSounds = new LinkedHashSet<String>(parsePathList(disabledSoundsNode));
		if (!this.disabledSounds.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			sb.append("Disabled sound configurations: ");
			int cnt = 0;
			for (String disabledSound : this.disabledSounds) {
				sb.append(disabledSound);
				cnt++;
				if (cnt != this.disabledSounds.size()) {
					sb.append(", ");
				}
			}
			logger.info(sb.toString());
		}

		Node mutedChannelsNode = configNodes.getElementsByTagName("mutedChannels").item(0);
		this.mutedChannels = new LinkedHashSet<String>(parsePathList(mutedChannelsNode));

		Node supplementalLogsNode = configNodes.getElementsByTagName("supplementalLogs").item(0);
		this.supplementalLogs = parsePathList(supplementalLogsNode);

	}

	private List<String> parsePathList(Node listNode) {
		List<String> list = new ArrayList<String>();

		NodeList items = listNode.getChildNodes();
		for (int j = 0; j < items.getLength(); j++) {
			Node item = items.item(j);
			String name = item.getLocalName();

			if ("item".equals(name)) {
				list.add(item.getAttributes().getNamedItem("path").getNodeValue());
			}
		}

		return list;
	}

	public void saveConfiguration() {
		logger.info("Saving configuration.");

		Element configNodes = (Element) doc.getElementsByTagName("configuration").item(0);

		Node gamelogNode = configNodes.getElementsByTagName("gamelog").item(0);
		gamelogNode.getAttributes().getNamedItem("path").setNodeValue(this.getGamelogPath());

		Node volumeNode = configNodes.getElementsByTagName("volume").item(0);
		volumeNode.getAttributes().getNamedItem("value").setNodeValue(Float.toString(this.getVolume()));

		Node playbackTheshholdNode = configNodes.getElementsByTagName("playbackTheshhold").item(0);
		playbackTheshholdNode.getAttributes().getNamedItem("value").setNodeValue(Long.toString(this.getPlaybackTheshhold()));

		Node autoUpdateReplaceFilesNode = configNodes.getElementsByTagName("autoUpdateReplaceFiles").item(0);
		autoUpdateReplaceFilesNode.getAttributes().getNamedItem("value").setNodeValue(Boolean.toString(this.getReplaceFiles()));

		Node autoUpdateDeleteFilesNode = configNodes.getElementsByTagName("autoUpdateDeleteFiles").item(0);
		autoUpdateDeleteFilesNode.getAttributes().getNamedItem("value").setNodeValue(Boolean.toString(this.getDeleteFiles()));

		Node achievementsNode = configNodes.getElementsByTagName("achievements").item(0);
		achievementsNode.getAttributes().getNamedItem("value").setNodeValue(Boolean.toString(this.getAchievements()));

		savePathList(configNodes, "disabledSounds", this.disabledSounds);
		savePathList(configNodes, "mutedChannels", this.mutedChannels);

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(this.doc), new StreamResult(new File(this.fileName)));
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	}

	private void savePathList(Element configNodes, String rootName, Set<String> items) {

		NodeList candidates = configNodes.getElementsByTagName(rootName);
		Node rootNode = null;
		if (candidates == null) {
			rootNode = configNodes.appendChild(doc.createElement(rootName));
		} else {
			rootNode = configNodes.getElementsByTagName(rootName).item(0);
		}

		NodeList childNodes = rootNode.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++) {
			rootNode.removeChild(childNodes.item(j));
		}
		for (String item : items) {
			Node newNode = rootNode.appendChild(doc.createElement("item"));
			Node path = doc.createAttribute("path");
			newNode.getAttributes().setNamedItem(path);
			path.setNodeValue(item);
		}
	}

	public String getGamelogPath() {
		return this.gamelogPath;
	}

	public void setGamelogPath(String gamelogPath) {
		this.gamelogPath = gamelogPath;
	}

	public String getSoundpacksPath() {
		return this.soundpacksPath;
	}

	public boolean isPackOverrideAllowed() {
		return this.allowPackOverride;
	}

	public boolean noWarnAbsolutePath() {
		return this.noWarnAbsolutePaths;
	}

	public float getVolume() {
		return this.volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	public boolean getGui() {
		return this.gui;
	}

	public List<String> getAutoUpdateURLs() {
		return autoUpdateURLs;
	}

	public void setPlaybackTheshhold(long playbackTheshhold) {
		this.playbackTheshhold = playbackTheshhold;
	}

	public long getPlaybackTheshhold() {
		return playbackTheshhold;
	}

	public final void setReplaceFiles(boolean replaceFiles) {
		this.replaceFiles = replaceFiles;
	}

	public boolean getReplaceFiles() {
		return replaceFiles;
	}

	public final void setDeleteFiles(boolean deleteFiles) {
		this.deleteFiles = deleteFiles;
	}

	public boolean getDeleteFiles() {
		return deleteFiles;
	}

	public String getGamelogEncoding() {
		return gamelogEncoding;
	}

	public Set<String> getDisabledSounds() {
		return disabledSounds;
	}

	public List<String> getSupplementalLogs() {
		return supplementalLogs;
	}

	public boolean getAchievements() {
		return achievements;
	}

	public void setAchievements(boolean achievements) {
		this.achievements = achievements;
	}

	public Set<String> getMutedChannels() {
		return mutedChannels;
	}

	public long getExpectedMinimalSoundsCount() {
		return expectedMinimalSoundsCount;
	}

}
