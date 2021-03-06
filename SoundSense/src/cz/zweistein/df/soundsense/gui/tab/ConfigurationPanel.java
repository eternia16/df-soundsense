package cz.zweistein.df.soundsense.gui.tab;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import cz.zweistein.df.soundsense.config.IReloadProgressCallback;
import cz.zweistein.df.soundsense.config.sounds.Attribution;
import cz.zweistein.df.soundsense.config.sounds.Sound;
import cz.zweistein.df.soundsense.config.sounds.SoundFile;
import cz.zweistein.df.soundsense.config.sounds.SoundsXML;
import cz.zweistein.df.soundsense.gui.Icons;
import cz.zweistein.df.soundsense.gui.adapter.ConfigurationXMLTreeAdapter;
import cz.zweistein.df.soundsense.gui.control.ProgressTicker;
import cz.zweistein.df.soundsense.gui.control.configuration.AttributionInfoPanel;
import cz.zweistein.df.soundsense.gui.control.configuration.IconTreeCellRenderer;
import cz.zweistein.df.soundsense.gui.control.configuration.SoundFileInfoPanel;
import cz.zweistein.df.soundsense.gui.control.configuration.SoundInfoPanel;
import cz.zweistein.df.soundsense.gui.control.configuration.XMLFileInfoPanel;
import cz.zweistein.df.soundsense.output.sound.SoundProcesor;

public class ConfigurationPanel extends JPanel implements ActionListener, IReloadProgressCallback {
	private static final long serialVersionUID = 7992706608900630741L;
	
	private ProgressTicker progressTicker;
	private JButton reloadButton;
	private SoundsXML soundsConfiguration;

	public ConfigurationPanel(SoundProcesor sp) {
		super();
		
		this.soundsConfiguration = sp.getSoundsXML();
		this.soundsConfiguration.registerReloadProgressCallback(this);
		
		this.setLayout(new BorderLayout());
		
		this.progressTicker = new ProgressTicker();
		
		JButton reloadButton = new JButton("Reload configuration");
		reloadButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(Icons.TRANSFER)));
		reloadButton.addActionListener(this);
		this.reloadButton = reloadButton;
		
		JPanel headerPanel = new JPanel();
		headerPanel.add(this.progressTicker);
		headerPanel.add(this.reloadButton);
        
		ConfigurationXMLTreeAdapter treeAdapter = new ConfigurationXMLTreeAdapter(this.soundsConfiguration);
		this.soundsConfiguration.registerReloadProgressCallback(treeAdapter);
        JTree soundsTree = new JTree(treeAdapter);
        soundsTree.setCellRenderer(new IconTreeCellRenderer(sp.getConfiguration()));
        
        JScrollPane soundsTreeScroller = new JScrollPane(soundsTree);
        
        JPanel infos = new JPanel();
        infos.setLayout(new BoxLayout(infos, BoxLayout.PAGE_AXIS));

        final SoundInfoPanel soundInfoPanel = new SoundInfoPanel(sp.getPlayerManager());
        soundInfoPanel.setVisible(false);
        infos.add(soundInfoPanel);
        
        final SoundFileInfoPanel soundFileInfoPanel = new SoundFileInfoPanel(sp.getPlayerManager());
        soundFileInfoPanel.setVisible(false);
        infos.add(soundFileInfoPanel);
        
        final XMLFileInfoPanel xmlFileInfoPanel = new XMLFileInfoPanel(sp.getConfiguration(), soundsConfiguration, soundsTree);
        xmlFileInfoPanel.setVisible(false);
        infos.add(xmlFileInfoPanel);
        
        final AttributionInfoPanel attributionInfoPanel = new AttributionInfoPanel();
        attributionInfoPanel.setVisible(false);
        infos.add(attributionInfoPanel);
        
        soundsTree.addTreeSelectionListener(new TreeSelectionListener() {
        	@Override
        	public void valueChanged(TreeSelectionEvent e) {
        		
        		if (e != null && e.getNewLeadSelectionPath() != null && e.getNewLeadSelectionPath().getLastPathComponent() != null) {
	        		Object selectedItem = e.getNewLeadSelectionPath().getLastPathComponent();
	        		
	        		if (selectedItem instanceof Sound) {
	        			
	        			soundFileInfoPanel.setVisible(false);
	        			xmlFileInfoPanel.setVisible(false);
	        			attributionInfoPanel.setVisible(false);
	        			
	        			soundInfoPanel.setVisible(true);
	        			soundInfoPanel.updateSound((Sound) selectedItem);
	        			
	        		} else if (selectedItem instanceof SoundFile) {
	        			
	        			soundInfoPanel.setVisible(false);
	        			xmlFileInfoPanel.setVisible(false);
	        			attributionInfoPanel.setVisible(false);
	        			
	        			soundFileInfoPanel.setVisible(true);
	        			soundFileInfoPanel.updateSoundFile((SoundFile) selectedItem);
	        			
	        		} else if (selectedItem instanceof String) {
	        			
	        			soundInfoPanel.setVisible(false);
	        			soundFileInfoPanel.setVisible(false);
	        			attributionInfoPanel.setVisible(false);
	        			
	        			xmlFileInfoPanel.setVisible(true);
	        			xmlFileInfoPanel.setPath((String) selectedItem);
	        			
	        		} else if (selectedItem instanceof Attribution) {
	        			
	        			soundInfoPanel.setVisible(false);
	        			soundFileInfoPanel.setVisible(false);
	        			xmlFileInfoPanel.setVisible(false);
	        			
	        			attributionInfoPanel.setVisible(true);
	        			attributionInfoPanel.setAttribution((Attribution)selectedItem);
						
					}
        		} else {
        			
        			soundInfoPanel.setVisible(false);
        			soundFileInfoPanel.setVisible(false);
        			xmlFileInfoPanel.setVisible(false);
        			
        		}
        	}
        });

        this.add(headerPanel, BorderLayout.PAGE_START);
        this.add(soundsTreeScroller, BorderLayout.CENTER);
        this.add(infos, BorderLayout.PAGE_END);
        
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		this.reloadButton.setEnabled(false);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				soundsConfiguration.reload();
			}
			
		}).start();

		
	}

	@Override
	public void done() {
		reloadButton.setEnabled(true);
	}

	@Override
	public void tick() {
		this.progressTicker.tick();
	}

}
