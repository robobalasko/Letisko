


import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.robobalasko.letisko.siet.AirportClient;

/**
 * Úvodná obrazovka klientskej časti aplikácie,
 * pomocou ktorej je možné v sieti vyhľadať server,
 * vybrať si letisko, na ktorom bude prebiehať simulácia a pripojiť sa.
 * 
 * @author rbalasko
 */
public class RadarIntroFrame extends JFrame {

    /**
     * Jazykové prostredie
     */
    private final Locale locale;
    
    /**
     * Načítaný preklad z properties súboru podľa jazykového prostredia
     */
    private final ResourceBundle language;
    
    /**
     * Načítané základné nastavenia aplikácie
     */
    private final ResourceBundle settings;
    
    /**
     * Letiskový klient objekt, ktorý zabezpečuje komunikáciu so serverom
     */
    private AirportClient airportClient;
    
    /**
     * Model pre JComboBox so zoznamom letísk, ku ktorým sa je možné pripojiť
     */
    private DefaultComboBoxModel airports;
    
    /**
     * ComboBox so zoznamom letísk, ku ktorým sa je možné pripojiť
     */
    private JComboBox airportsList;
    
    /**
     * Tlačítko na pripojenie k serveru a zapnutie radaru
     */
    private JButton buttonConnect;
    
    /**
     * Základný konštruktor rozmiestňuje komponenty
     * pre úvodné okno aplikácie.
     * 
     * Tlačítko na vyhľadanie servera ostáva neaktívne
     * kým sa do poľa pre IP adresu servera nevpíše aspoň
     * 7 znakov, ktoré zodpovedajú IP adrese 0.0.0.0 pri pripájaní
     * sa na lokálnom počítači.
     * 
     * Po vyhľadaní servera v sieti sa odošle žiadosť o zoznam
     * dostupných letísk, ku ktorým sa klienti môžu pripájať.
     * Po výbere letiska sa serveru znova odosiela žiadosť s ICAO kódom
     * letiska, ku ktorému sa chce klient pripojiť.
     * 
     * Pred otvorením obrazovky s radarom sa aplikácií zo serveru pošle
     * objekt {@code Airport}, ktorý sa predá radarovému oknu a okno sa vytvorí.
     * 
     */
    public RadarIntroFrame() {
        super();
        
        // Nastavenie základných resources pre potreby programu
        this.locale = Locale.getDefault();
        this.language = ResourceBundle.getBundle("languages/language", locale);
        this.settings = ResourceBundle.getBundle("config/settings");
        
        // Základné nastavenie okna
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(language.getString("language.title"));
        Container obsah = getContentPane();
        
        // Zadný JPanel s obrázkom v pozadí
        ImageJPanel panelMain = new ImageJPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.PAGE_AXIS));
        obsah.add(panelMain);
        
        // Panel pre zadanie adresy servera
        JPanel panelConnect = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelConnect.setOpaque(false);
        panelConnect.setBorder(new EmptyBorder(160, 0, 0, 0));
        panelMain.add(panelConnect);
        
        // Riadok pre zadanie adresy servera
        final JTextField textFieldAddress = new JTextField(15);
        textFieldAddress.setText("0.0.0.0");
        textFieldAddress.setBorder(new LineBorder(Color.BLACK, 1));
        textFieldAddress.setSize(200, 50);
        panelConnect.add(textFieldAddress);
        
        // Tlačítko pripojenia sa k serveru
        final JButton buttonSearch = new JButton(language.getString("language.button_search"));
        textFieldAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (textFieldAddress.getText().length() >= 6
                        && textFieldAddress.getText().length() <= 15) {
                    buttonSearch.setEnabled(true);
                } else {
                    buttonSearch.setEnabled(false);
                }
            }
        });
        
        // Hľadanie servera a pripojenie
        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> requestedList = null;
                try {
                    String hostName = textFieldAddress.getText();
                    int hostPort = Integer.parseInt(settings.getString("settings.port"));
                    airportClient = new AirportClient(hostName, hostPort);
                    requestedList = airportClient.requestAirportsList();
                } catch (IOException ex) {
					JOptionPane.showMessageDialog(RadarIntroFrame.this,
							"Požadovaný server sa v sieti nenachádza!",
							"Chyba spojenia", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(RadarIntroFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                
				if (requestedList != null) {
					airports.removeAllElements();
					for (String airport : requestedList) {
						airports.addElement(airport.toUpperCase());
					}
					airportsList.setEnabled(true);
				}
            }
        });
        panelConnect.add(buttonSearch);
        
        // Panel s výberom letiska
        JPanel panelSelect = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSelect.setOpaque(false);
        panelMain.add(panelSelect);
        airports = new DefaultComboBoxModel(new String[] {language.getString("language.select")});
        airportsList = new JComboBox(airports);
        airportsList.setEnabled(false);
        airportsList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonConnect.setEnabled(true);
            }
        });
        panelSelect.add(airportsList);
        
        // Panel s tlačítkami volieb aplikácie
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelButtons.setOpaque(false);
        panelMain.add(panelButtons);
        buttonConnect = new JButton(language.getString("language.button_select"));
        buttonConnect.setEnabled(false);
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String airportIcaoCode = (String) airports.getSelectedItem();
                RadarControllerScreenFrame radarGUI
                        = new RadarControllerScreenFrame(airportIcaoCode.toLowerCase(), airportClient, language);
				setVisible(false);
            }
        });
        panelButtons.add(buttonConnect);
        
        // Zavretie aplikácie
        JButton buttonStorno = new JButton(language.getString("language.button_close"));
        buttonStorno.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RadarIntroFrame.this.dispose();
                System.exit(0);
            }
        });
        panelButtons.add(buttonStorno);
        setSize(640, 480);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
}
