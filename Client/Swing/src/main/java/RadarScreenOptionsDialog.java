


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Dialógové okno, pomocou ktorého je možné meniť
 * nastavenia na radarovej obrazovke.
 * 
 * @author rbalasko
 */
public class RadarScreenOptionsDialog extends JDialog {
    
    /**
     * Súbor s jazykovým prekladom aplikácie.
     */
    private final ResourceBundle language;
    
    /**
     * Objekt s nastaveniami radarovej obrazovky.
     */
    private final RadarScreenOptions radarScreenOptions;
    
    /**
     * CheckBox pre nastavenia zobrazenia názvov
     * letových bodov na radare.
     */
    JCheckBox checkPointNames;
    
    /**
     * CheckBox pre nastavenie zobrazenia GPS súradníc
     * letových bodov na radare.
     */
    JCheckBox checkPointGPS;
    
    /**
     * CheckBox pre nastavenie zobrazenia frekvencia
     * bodov typu VOR na radare.
     */
    JCheckBox checkPointFreq;
    
    /**
     * CheckBox pre nastavenia zobrazenia diľkových
     * kruhov od stredovej pozície letiska.
     */
    JCheckBox checkCircles;
	
	/**
	 * Checkbox pre nastavenia zobrazenia typu
	 * lietadla na radare.
	 */
	JCheckBox checkAircraftType;
	
	/**
	 * Checkbox pre nastavenie zobrazenia aktuálnej
	 * výšky letu lietadla na radare.
	 */
	JCheckBox checkAircraftActualFlightLevel;
	
	/**
	 * Checkbox pre nastavenie zobrazenia maximálne
	 * povolenej výšky lietadla na radare.
	 */
	JCheckBox checkAircraftFinalFlightLevel;
	
	/**
	 * Checkbox pre nastavenie zobrazenia aktuálnej
	 * rýchlosti lietadla na radare.
	 */
	JCheckBox checkAircraftActualSpeed;
	
	/**
	 * Checkbox pre nastavenie zobrazenia maximálne
	 * povolenej výšky lietadla na radare.
	 */
	JCheckBox checkAircraftFinalSpeed;

    /**
     * Základný konštruktor vytvára celé okno dialógu
     * a zobrazí aktuálne navolené nastavenia pre zobrazovanie
     * údajov na radarovej obrazovke
     * 
     * @param parent Rodičovské okno, z ktorého sa dialóg volá
     * @param language Súbor s jazykovým prekladom aplikácie
     * @param radarScreenOptions Objekt s nastaveniami radarovej obrazovky
     */
    public RadarScreenOptionsDialog(final JFrame parent, ResourceBundle language,
            RadarScreenOptions radarScreenOptions) {
        super(parent, language.getString("language.options"), true);
        this.language = language;
        this.radarScreenOptions = radarScreenOptions;
        Container content = getContentPane();
		FlowLayout leftFlowLayoutStyle = new FlowLayout(FlowLayout.LEFT);
        
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        content.add(panelInfo, BorderLayout.NORTH);
        
        JLabel labelInfo = new JLabel(language.getString("language.options_window_title"));
        panelInfo.add(labelInfo);
        
        JPanel panelCheckBoxes = new JPanel();
        panelCheckBoxes.setLayout(new BoxLayout(panelCheckBoxes, BoxLayout.PAGE_AXIS));
        content.add(panelCheckBoxes, BorderLayout.CENTER);
        
        JPanel panelCheckPointNames = new JPanel(leftFlowLayoutStyle);
        panelCheckBoxes.add(panelCheckPointNames);
        
        checkPointNames = new JCheckBox(language.getString("language.show_wpt_names"));
        if (this.radarScreenOptions.isDispPointNames()) {
            checkPointNames.setSelected(true);
        }
        panelCheckPointNames.add(checkPointNames);
        
        JPanel panelCheckPointGPS = new JPanel(leftFlowLayoutStyle);
        panelCheckBoxes.add(panelCheckPointGPS);
        
        checkPointGPS = new JCheckBox(language.getString("language.show_wpt_gps"));
        if (this.radarScreenOptions.isDispPointGps()) {
            checkPointGPS.setSelected(true);
        }
        panelCheckPointGPS.add(checkPointGPS);
        
        JPanel panelCheckPointFreq = new JPanel(leftFlowLayoutStyle);
        panelCheckBoxes.add(panelCheckPointFreq);
        
        checkPointFreq = new JCheckBox(language.getString("language.show_vor_freq"));
        if (this.radarScreenOptions.isDispPointFreq()) {
            checkPointFreq.setSelected(true);
        }
        panelCheckPointFreq.add(checkPointFreq);
        
        JPanel panelCheckCircles = new JPanel(leftFlowLayoutStyle);
        panelCheckBoxes.add(panelCheckCircles);
        
        checkCircles = new JCheckBox(language.getString("language.show_dist_circles"));
        if (this.radarScreenOptions.isDispDistCircles()) {
            checkCircles.setSelected(true);
        }
        panelCheckCircles.add(checkCircles);
		
		JPanel panelCheckAircraftType = new JPanel(leftFlowLayoutStyle);
		panelCheckBoxes.add(panelCheckAircraftType);
		
		checkAircraftType = new JCheckBox(language.getString("language.show_acft_type"));
		if (this.radarScreenOptions.isDispAircraftType()) {
			checkAircraftType.setSelected(true);
		}
		panelCheckAircraftType.add(checkAircraftType);
		
		JPanel panelCheckAircraftActualSpeed = new JPanel(leftFlowLayoutStyle);
		panelCheckBoxes.add(panelCheckAircraftActualSpeed);
		
		checkAircraftActualSpeed = new JCheckBox(language.getString("language.show_act_speed"));
		if (this.radarScreenOptions.isDispAircraftActualSpeed()) {
			checkAircraftActualSpeed.setSelected(true);
		}
		panelCheckAircraftActualSpeed.add(checkAircraftActualSpeed);
		
		JPanel panelCheckAircraftFinalSpeed = new JPanel(leftFlowLayoutStyle);
		panelCheckBoxes.add(panelCheckAircraftFinalSpeed);
		
		checkAircraftFinalSpeed = new JCheckBox(language.getString("language.show_fin_speed"));
		if (this.radarScreenOptions.isDispAircraftFinalSpeed()) {
			checkAircraftFinalSpeed.setSelected(true);
		}
		panelCheckAircraftFinalSpeed.add(checkAircraftFinalSpeed);
		
		JPanel panelCheckAircraftActualFlightLevel = new JPanel(leftFlowLayoutStyle);
		panelCheckBoxes.add(panelCheckAircraftActualFlightLevel);
		
		checkAircraftActualFlightLevel = new JCheckBox(language.getString("language.show_act_flevel"));
		if (this.radarScreenOptions.isDispAircraftActualFlightLevel()) {
			checkAircraftActualFlightLevel.setSelected(true);
		}
		panelCheckAircraftActualFlightLevel.add(checkAircraftActualFlightLevel);
		
		JPanel panelCheckAircraftFinalFlightLevel = new JPanel(leftFlowLayoutStyle);
		panelCheckBoxes.add(panelCheckAircraftFinalFlightLevel);
		
		checkAircraftFinalFlightLevel = new JCheckBox(language.getString("language.show_fin_flevel"));
		if (this.radarScreenOptions.isDispAircraftFinalFlightLevel()) {
			checkAircraftFinalFlightLevel.setSelected(true);
		}
		panelCheckAircraftFinalFlightLevel.add(checkAircraftFinalFlightLevel);
        
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        content.add(panelButtons, BorderLayout.SOUTH);
        
        JButton buttonOK = new JButton(language.getString("language.button_apply"));
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adjustRadarScreenSettings();
                parent.repaint();
                dispose();
            }
        });
        panelButtons.add(buttonOK);
        
        JButton buttonStorno = new JButton(language.getString("language.button_close"));
        buttonStorno.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        panelButtons.add(buttonStorno);
        
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
    
    /**
     * Metóda mení nastavenia atribútov v objekte nastavení
     * {@code RadarScreenOptions} podľa toho, ktoré JCheckBox
     * sú pri aktuálnej zmene nastavenia zaškrtnuté.
     * 
     * @see RadarScreenOptions
     */
    public void adjustRadarScreenSettings() {
        radarScreenOptions.setDispPointNames(checkPointNames.isSelected());
        radarScreenOptions.setDispPointGps(checkPointGPS.isSelected());
        radarScreenOptions.setDispPointFreq(checkPointFreq.isSelected());
        radarScreenOptions.setDispDistCircles(checkCircles.isSelected());
		radarScreenOptions.setDispAircraftType(checkAircraftType.isSelected());
		radarScreenOptions.setDispAircraftActualSpeed(checkAircraftActualSpeed.isSelected());
		radarScreenOptions.setDispAircraftFinalSpeed(checkAircraftFinalSpeed.isSelected());
		radarScreenOptions.setDispAircraftActualFlightLevel(checkAircraftActualFlightLevel.isSelected());
		radarScreenOptions.setDispAircraftFinalFlightLevel(checkAircraftFinalFlightLevel.isSelected());
    }
    
}
