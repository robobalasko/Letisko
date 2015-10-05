
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import net.robobalasko.letisko.siet.AircraftHandler;
import net.robobalasko.letisko.siet.AirportClient;
import net.robobalasko.letiskoserv.lietadla.Aircraft;
import net.robobalasko.letiskoserv.navigacia.Airport;

/**
 * Trieda radarovej obrazovky, s ktorou interaguje klient počas behu simulácie.
 *
 * @author rbalasko
 */
public class RadarControllerScreenFrame extends JFrame {

    /**
     * Jazykový preklad aplikácie.
     */
    private final ResourceBundle language;

    /**
     * Handler, ktorý sa stará o lietadlá nachádzajúce sa na letisku.
     */
    private final AircraftHandler aircraftHandler;

    /**
     * Objekt, ktorý komunikuje so stranou klienta.
     */
    private final AirportClient airportClient;

    /**
     * Zoznam lietadiel, ktoré boli riadiacim na letisku modifikované.
     */
    private final List<Aircraft> modifiedAircraft;

    /**
     * Hlavný kontainer okna, do ktorého sa pridávajú elementy.
     */
    private final Container content;

    /**
     * Radarová obrazovka, na ktorej sa zobrazuje aktuálny stav na letisku.
     */
    private RadarScreen radarScreen;

    /**
     * Objekt s nastaveniami radarovej obrazovky.
     */
    private RadarScreenOptions radarScreenOptions;

    /**
     * Zoznam s lietadlami, ktoré odlietajú z tohto letiska
     */
    private RadarAircraftJList aircraftDepsList;

    private RadarAircraftJList aircraftArrsList;

    private final JLabel titleLabel;

    /**
     * Label v info panely lietadla, do ktorého sa vypíše jeho volací znak po vybratí položky v JListe odletov/príletov.
     */
    private JLabel callSign;

    /**
     * Label v info panely lietadla, do ktorého sa vypíše jeho typ znak po vybratí položky v JListe odletov/príletov.
     */
    private final JLabel acftType;

    private final JLabel acftDepArpt;

    private final JLabel acftArrArpt;

    private final JLabel acftFinalFl;

    private final JLabel acftActualFl;

    private final JLabel acftFinalSpeed;

    private final JLabel acftActualSpeed;

    private final JLabel acftSidRoute;

    private final JLabel acftStarRoute;

    /**
     * Základný konštruktor rozmiestňuje komponenty v okne.
     *
     * @param airportIcaoCode ICAO kód letiska, ktoré sa pripája k serveru
     * @param airportClient Objekt, ktorý komunikuje so stranou servera
     * @param language Jazykový preklad aplikácie
     */
    public RadarControllerScreenFrame(final String airportIcaoCode, final AirportClient airportClient,
            ResourceBundle language) {
        super();
        this.language = language;
        this.airportClient = airportClient;
        modifiedAircraft = new LinkedList<Aircraft>();
        this.aircraftHandler = new AircraftHandler(airportClient, modifiedAircraft);
        content = getContentPane();

        // Panel s nadpisom okna
        JPanel titlePanel = new JPanel();
        content.add(titlePanel, BorderLayout.NORTH);

        // Nadpis okna
        titleLabel = new JLabel();
        titlePanel.add(titleLabel, BorderLayout.PAGE_START);

        // Vlastný JLabel s aktuálnym GMT časom
        ClockJLabel clockLabel = new ClockJLabel();
        titlePanel.add(clockLabel, BorderLayout.PAGE_END);

        prepareRadarScreen(content);

        // Panel s možnosťami pre radarovú obrazovku
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
        optionsPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        content.add(optionsPanel, BorderLayout.WEST);

        // Nadpis pre sekciu s lietadlami na letisku
        JLabel aircraftTitleLabel
                = new JLabel(RadarControllerScreenFrame.this.language.getString("language.acfts_at_arpt"));
        optionsPanel.add(aircraftTitleLabel);

        setUpAircraftDepsList(optionsPanel, airportIcaoCode);
        setUpAircraftArrsList(optionsPanel, airportIcaoCode);

        // Sekcia s informáciami o lete
        JPanel aircraftInfoPanel = new JPanel();
        aircraftInfoPanel.setLayout(new BoxLayout(aircraftInfoPanel, BoxLayout.PAGE_AXIS));
        aircraftInfoPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        //content.add(aircraftInfoPanel, BorderLayout.EAST);

        // Nadpis pre sekciu s informáciami o lete
        JLabel aircraftInfoTitle
                = new JLabel(RadarControllerScreenFrame.this.language.getString("language.flight_info"));
        aircraftInfoPanel.add(aircraftInfoTitle);

        JPanel panelAcftCallSign = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aircraftInfoPanel.add(panelAcftCallSign);

        JLabel labelCallSign = new JLabel("Volací znak: ");
        panelAcftCallSign.add(labelCallSign);

        callSign = new JLabel();
        panelAcftCallSign.add(callSign);

        JPanel panelAcftType = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aircraftInfoPanel.add(panelAcftType);

        JLabel labelAcftType = new JLabel("Typ lietadla: ");
        panelAcftType.add(labelAcftType);

        acftType = new JLabel();
        panelAcftType.add(acftType);

        JPanel panelAcftDepArpt = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aircraftInfoPanel.add(panelAcftDepArpt);

        JLabel labelAcftDepArpt = new JLabel("Letisko odletu: ");
        panelAcftDepArpt.add(labelAcftDepArpt);

        acftDepArpt = new JLabel();
        panelAcftDepArpt.add(acftDepArpt);

        JPanel panelAcftArrArpt = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aircraftInfoPanel.add(panelAcftArrArpt);

        JLabel labelAcftArrArpt = new JLabel("Letisko príletu:");
        panelAcftArrArpt.add(labelAcftArrArpt);

        acftArrArpt = new JLabel();
        panelAcftArrArpt.add(acftArrArpt);

        JPanel panelAcftFinalFl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aircraftInfoPanel.add(panelAcftFinalFl);

        JLabel labelAcftFinalFl = new JLabel("Žiadaná výška: ");
        panelAcftFinalFl.add(labelAcftFinalFl);

        acftFinalFl = new JLabel();
        panelAcftFinalFl.add(acftFinalFl);

        JPanel panelAcftActualFl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aircraftInfoPanel.add(panelAcftActualFl);

        JLabel labelAcftActualFl = new JLabel("Aktuálna výška: ");
        panelAcftActualFl.add(labelAcftActualFl);

        acftActualFl = new JLabel();
        panelAcftActualFl.add(acftActualFl);

        JPanel panelAcftFinalSpeed = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aircraftInfoPanel.add(panelAcftFinalSpeed);

        JLabel labelAcftFinalSpeed = new JLabel("Žiadaná rýchlosť: ");
        panelAcftFinalSpeed.add(labelAcftFinalSpeed);

        acftFinalSpeed = new JLabel();
        panelAcftFinalSpeed.add(acftFinalSpeed);

        JPanel panelAcftActualSpeed = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aircraftInfoPanel.add(panelAcftActualSpeed);

        JLabel labelAcftActualSpeed = new JLabel("Aktuálna rýchlosť: ");
        panelAcftActualSpeed.add(labelAcftActualSpeed);

        acftActualSpeed = new JLabel();
        panelAcftActualSpeed.add(acftActualSpeed);

        JPanel panelAcftSidRoute = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aircraftInfoPanel.add(panelAcftSidRoute);

        JLabel labelAcftSidRoute = new JLabel("Trasa SID: ");
        panelAcftSidRoute.add(labelAcftSidRoute);

        acftSidRoute = new JLabel();
        panelAcftSidRoute.add(acftSidRoute);

        JPanel panelAcftStarRoute = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aircraftInfoPanel.add(panelAcftStarRoute);

        JLabel labelAcftStarRoute = new JLabel("Trasa STAR: ");
        panelAcftStarRoute.add(labelAcftStarRoute);

        acftStarRoute = new JLabel();
        panelAcftStarRoute.add(acftStarRoute);

        // Sekcia s tlačítkami volieb aplikácie
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        content.add(buttonsPanel, BorderLayout.SOUTH);

        // Nastavenia radaru
        JButton buttonOptions
                = new JButton(RadarControllerScreenFrame.this.language.getString("language.options"));
        buttonOptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RadarScreenOptionsDialog optionsGUI
                        = new RadarScreenOptionsDialog(RadarControllerScreenFrame.this,
                                RadarControllerScreenFrame.this.language,
                                RadarControllerScreenFrame.this.radarScreenOptions);
            }
        });
        buttonsPanel.add(buttonOptions);

        // Ukončenie simulácie
        JButton buttonClose = new JButton(language.getString("language.button_close"));
        buttonClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RadarControllerScreenFrame.this.setVisible(false);
                aircraftHandler.stop();
                RadarControllerScreenFrame.this.airportClient.endServerCommunication();
                System.exit(0);
            }
        });
        buttonsPanel.add(buttonClose);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                Airport airport = airportClient.requestAirportData(airportIcaoCode,
                        radarScreen.getWidth(), radarScreen.getHeight());
                radarScreen.setAirportData(airport);
                aircraftHandler.start();
                String title = airport.getIcaoCode()
                        + " - " + airport.getAirportName();
                RadarControllerScreenFrame.this.setTitle(title);
                titleLabel.setText(title);
            }
        });

        setSize(1200, 600);
        setVisible(true);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void prepareRadarScreen(Container content) {
        radarScreenOptions = new RadarScreenOptions();
        radarScreen = new RadarScreen(RadarControllerScreenFrame.this,
                radarScreenOptions, aircraftHandler, airportClient, modifiedAircraft);
        content.add(radarScreen, BorderLayout.CENTER);
    }

    private void setUpAircraftDepsList(JPanel optionsPanel, String airportIcaoCode) {
        // Nadpis pre sekciu s odletmi
        JLabel aircraftDepsLabel = new JLabel(language.getString("language.departures"));
        optionsPanel.add(aircraftDepsLabel);

        // Zoznam odlietajúcich lietadiel
        aircraftDepsList = new RadarAircraftJList(aircraftHandler, airportIcaoCode,
                AircraftDataDisplayTypeEnum.DEPARTURE);
        JScrollPane scrollerDepsList = new JScrollPane(aircraftDepsList);
        optionsPanel.add(scrollerDepsList);
    }
    
    private void setUpAircraftArrsList(JPanel optionsPanel, String airportIcaoCode) {
        JLabel aircraftArrsLabel = new JLabel(language.getString("language.arrivals"));
        optionsPanel.add(aircraftArrsLabel);
        
        aircraftArrsList = new RadarAircraftJList(aircraftHandler, airportIcaoCode,
                AircraftDataDisplayTypeEnum.ARRIVAL);
        JScrollPane scrollerArrsList = new JScrollPane(aircraftArrsList);
        optionsPanel.add(scrollerArrsList);
    }

}
