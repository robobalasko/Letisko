
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.robobalasko.letiskoserv.lietadla.Aircraft;
import net.robobalasko.letiskoserv.navigacia.Airport;
import net.robobalasko.letiskoserv.navigacia.AirportRouteTypesEnum;
import net.robobalasko.letiskoserv.navigacia.RouteNotLoadedException;

public class AircraftControllerCommandsJDialog extends JDialog {

    /**
     * Slider na nastavenie maximálnej možnej letovej výšky lietadla.
     */
    private final JSlider sliderFlevel;

    /**
     * Slider na nastavenie maximálnej možnej letovej rýchlosti lietadla.
     */
    private final JSlider sliderSpeed;

    /**
     * Label na zobrazenie aktuálneho nastavenie letovej výšky.
     */
    private final JLabel labelFlevelActual;

    /**
     * Label na zobrazenie aktuálneho nastavenia letovej rýchlosti.
     */
    private final JLabel labelSpeedActual;

    /**
     * ComboBox so všetkými bodmi letiska, ktoré sa môžu použiť
     * ako vektor pre zmenu trasy lietadla za letu.
     */
    private final JComboBox comboNextWaypoint;
    
    /**
     * CheckBox ktorý definuje, či riadiaci ide zmeniť lietadlu trasu.
     */
    private final JCheckBox checkBoxChangeRoute;
    
    /**
     * CheckBox ktorý definuje, či sa pri zmene trasy na nejaký samostatný
     * vektor smerom k istému bodu má celá predošlá trasa zmazať.
     */
    private final JCheckBox checkBoxClearList;
    
    /**
     * ComboBox so všetkými dostupnými odletovými trasami,
     * ktoré je možné lietadlu priradiť.
     */
    private final JComboBox comboSidRoutePoints;
    
    /**
     * ComboBox so všetkými dostupnými príletovými trasami,
     * ktoré je možné lietadlu priradiť.
     */
    private final JComboBox comboStarRoutePoints;
    
    /**
     * CheckBox ktorý definuje, či má byť lietadlu povolený odlet.
     */
    private JCheckBox checkBoxClearedDeparture;
    
    /**
     * CheckBox ktorý definuje, či sa má zmeniť odletová trasa lietadla.
     */
    private final JCheckBox checkBoxChangeSid;
    
    /**
     * CheckBox ktorý definuje, či sa má zmeniť príletová trasa lietadla.
     */
    private final JCheckBox checkBoxChangeStar;

    public AircraftControllerCommandsJDialog(Frame parent,
            final Aircraft aircraft, final List<Aircraft> modifiedAircraft,
            final RadarScreen radarScreen, final Airport airportData) {
        super(parent, "Riadiaci panel letu: " + aircraft.getCallSign());
        Container content = getContentPane();
        Font monoFontStyle = new Font(Font.MONOSPACED, Font.BOLD, 12);
        AirportRouteTypesEnum aircraftRouteType = aircraft.getActualRoute().getRouteType();

        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.PAGE_AXIS));
        content.add(panelHeader, BorderLayout.PAGE_START);

        JPanel panelTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelHeader.add(panelTitle);

        JLabel labelTitle = new JLabel("Riadiaci panel letu: " + aircraft.getCallSign());
        panelTitle.add(labelTitle);

        JPanel panelRoute = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelHeader.add(panelRoute);

        JLabel labelRoute = new JLabel(aircraft.getDepAirport().toUpperCase()
                + " -> " + aircraft.getArrAirport().toUpperCase());
        labelRoute.setFont(monoFontStyle);
        panelRoute.add(labelRoute);
        
        JPanel panelRoutes = new JPanel();
        panelRoutes.setLayout(new BoxLayout(panelRoutes, BoxLayout.LINE_AXIS));
        panelHeader.add(panelRoutes);
        
        JPanel panelSidRouteMain = new JPanel();
        panelSidRouteMain.setLayout(new BoxLayout(panelSidRouteMain, BoxLayout.PAGE_AXIS));
        panelRoutes.add(panelSidRouteMain);

        JPanel panelSidRoute = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSidRouteMain.add(panelSidRoute);

        JLabel labelSidTitle = new JLabel("DEP");
        labelSidTitle.setFont(monoFontStyle);
        labelSidTitle.setForeground(Color.RED);
        //panelSidRoute.add(labelSidTitle);

        JPanel panelSidRoutePoints = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSidRouteMain.add(panelSidRoutePoints);
        panelSidRoutePoints.add(labelSidTitle);

        checkBoxChangeSid = new JCheckBox("Zmeniť");
        if (aircraftRouteType == AirportRouteTypesEnum.SID) {
            checkBoxChangeSid.setEnabled(true);
        } else {
            checkBoxChangeSid.setEnabled(false);
        }
        checkBoxChangeSid.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                comboSidRoutePoints.setEnabled(checkBoxChangeSid.isSelected());
            }
        });
        panelSidRoutePoints.add(checkBoxChangeSid);
        
        comboSidRoutePoints = new JComboBox();
        Object[] sidRoute;
        if (aircraft.getActualRoute().getRouteType() == AirportRouteTypesEnum.SID) {
            sidRoute = airportData.getSpecificRoutes(AirportRouteTypesEnum.SID).toArray();
        } else {
            sidRoute = new String[] {aircraft.getSidRoute().getRouteName()};
        }
        comboSidRoutePoints.setModel(new DefaultComboBoxModel(sidRoute));
        comboSidRoutePoints.setSelectedItem(aircraft.getSidRoute().getRouteName());
        comboSidRoutePoints.setEnabled(false);
        comboSidRoutePoints.setFont(monoFontStyle);
        panelSidRoutePoints.add(comboSidRoutePoints);
        
        JPanel panelStarRouteMain = new JPanel();
        panelStarRouteMain.setLayout(new BoxLayout(panelStarRouteMain, BoxLayout.PAGE_AXIS));
        panelStarRouteMain.setMinimumSize(new Dimension(this.getWidth() / 2, panelSidRouteMain.getHeight()));
        panelRoutes.add(panelStarRouteMain);

        JPanel panelStarRoute = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelStarRouteMain.add(panelStarRoute);

        JLabel labelStarTitle = new JLabel("ARR");
        labelStarTitle.setFont(monoFontStyle);
        labelStarTitle.setForeground(Color.RED);
        //panelStarRoute.add(labelStarTitle);

        JPanel panelStarRoutePoints = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelStarRouteMain.add(panelStarRoutePoints);
        panelStarRoutePoints.add(labelStarTitle);
        
        checkBoxChangeStar = new JCheckBox("Zmeniť");
        if (aircraftRouteType == AirportRouteTypesEnum.STAR) {
            checkBoxChangeStar.setEnabled(true);
        } else {
            checkBoxChangeStar.setEnabled(false);
        }
        checkBoxChangeStar.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                comboStarRoutePoints.setEnabled(checkBoxChangeStar.isSelected());
            }
        });
        
        panelStarRoutePoints.add(checkBoxChangeStar);

        comboStarRoutePoints = new JComboBox();
        Object[] starRoute;
        if (aircraft.getActualRoute().getRouteType() == AirportRouteTypesEnum.STAR) {
            starRoute = airportData.getSpecificRoutes(AirportRouteTypesEnum.STAR).toArray();
        } else {
            starRoute = new String[] {aircraft.getStarRoute().getRouteName()};
        }
        comboStarRoutePoints.setModel(new DefaultComboBoxModel(starRoute));
        comboStarRoutePoints.setSelectedItem(aircraft.getStarRoute().getRouteName());
        comboStarRoutePoints.setEnabled(false);
        comboStarRoutePoints.setFont(monoFontStyle);
        panelStarRoutePoints.add(comboStarRoutePoints);
        
        JPanel panelCenterMain = new JPanel();
        panelCenterMain.setLayout(new BoxLayout(panelCenterMain, BoxLayout.PAGE_AXIS));
        content.add(panelCenterMain);
        
        if (!aircraft.isClearedForDeparture()) {
            JPanel panelClearedDeparture = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panelCenterMain.add(panelClearedDeparture);

            checkBoxClearedDeparture = new JCheckBox("Povoliť letu " + aircraft.getCallSign() + " vzlet");
            panelClearedDeparture.add(checkBoxClearedDeparture);
        }

        JPanel panelControls = new JPanel();
        panelControls.setLayout(new BoxLayout(panelControls, BoxLayout.LINE_AXIS));
        panelCenterMain.add(panelControls);

        JPanel controlsPanelFlevel = new JPanel();
        controlsPanelFlevel.setLayout(new BoxLayout(controlsPanelFlevel, BoxLayout.PAGE_AXIS));
        controlsPanelFlevel.setBorder(new EmptyBorder(0, 0, 0, 10));
        panelControls.add(controlsPanelFlevel);

        panelControls.add(new JSeparator(JSeparator.VERTICAL));

        JPanel panelFlevelTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlsPanelFlevel.add(panelFlevelTitle);

        JLabel labelFlevelTitle = new JLabel("Letová hladina:");
        panelFlevelTitle.add(labelFlevelTitle);

        JPanel panelFlevelActual = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlsPanelFlevel.add(panelFlevelActual);

        labelFlevelActual = new JLabel(String.valueOf(aircraft.getFinalFlightLevel()));
        labelFlevelActual.setForeground(Color.BLUE);
        panelFlevelActual.add(labelFlevelActual);

        JPanel panelFlevelControl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlsPanelFlevel.add(panelFlevelControl);

        sliderFlevel = new JSlider(10, 160, aircraft.getFinalFlightLevel());
        sliderFlevel.setMajorTickSpacing(50);
        sliderFlevel.setMinorTickSpacing(10);
        sliderFlevel.setPaintTicks(true);
        sliderFlevel.setPaintLabels(true);
        sliderFlevel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                labelFlevelActual.setText(String.valueOf(sliderFlevel.getValue()));
            }
        });
        panelFlevelControl.add(sliderFlevel);

        JPanel controlsPanelSpeed = new JPanel();
        controlsPanelSpeed.setLayout(new BoxLayout(controlsPanelSpeed, BoxLayout.PAGE_AXIS));
        controlsPanelSpeed.setBorder(new EmptyBorder(0, 10, 0, 0));
        panelControls.add(controlsPanelSpeed);

        JPanel panelSpeedTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlsPanelSpeed.add(panelSpeedTitle);

        JLabel labelSpeedTitle = new JLabel("Max. rýchlosť:");
        panelSpeedTitle.add(labelSpeedTitle);

        JPanel panelSpeedActual = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlsPanelSpeed.add(panelSpeedActual);

        labelSpeedActual = new JLabel(String.valueOf(aircraft.getFinalAirSpeed()));
        labelSpeedActual.setForeground(Color.BLUE);
        panelSpeedActual.add(labelSpeedActual);

        JPanel panelSpeedControl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlsPanelSpeed.add(panelSpeedControl);

        sliderSpeed = new JSlider(100, 300, aircraft.getFinalAirSpeed());
        sliderSpeed.setMajorTickSpacing(50);
        sliderSpeed.setMinorTickSpacing(10);
        sliderSpeed.setPaintTicks(true);
        sliderSpeed.setPaintLabels(true);
        sliderSpeed.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                labelSpeedActual.setText(String.valueOf(sliderSpeed.getValue()));
            }
        });
        panelSpeedControl.add(sliderSpeed);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));
        content.add(bottomPanel, BorderLayout.PAGE_END);

        JPanel panelNextWaypoint = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(panelNextWaypoint);
        
        checkBoxChangeRoute = new JCheckBox("Zmeniť trasu:");
        checkBoxChangeRoute.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                AirportRouteTypesEnum actualRouteType = aircraft.getActualRoute().getRouteType();
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    checkBoxChangeSid.setSelected(false);
                    checkBoxChangeSid.setEnabled(false);
                    checkBoxChangeStar.setSelected(false);
                    checkBoxChangeStar.setEnabled(false);
                    checkBoxClearList.setEnabled(true);
                    comboNextWaypoint.setEnabled(true);
                }
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    if (actualRouteType == AirportRouteTypesEnum.SID) {
                        checkBoxChangeSid.setEnabled(true);
                    } else {
                        checkBoxChangeStar.setEnabled(true);
                    }
                    checkBoxClearList.setEnabled(false);
                    comboNextWaypoint.setEnabled(false);
                }
            }
        });
        panelNextWaypoint.add(checkBoxChangeRoute);

        comboNextWaypoint = new JComboBox(airportData.getWaypoinst().toArray());
        comboNextWaypoint.setEnabled(false);
        panelNextWaypoint.add(comboNextWaypoint);
        
        checkBoxClearList =  new JCheckBox("Vymazať predošlé body");
        checkBoxClearList.setEnabled(false);
        panelNextWaypoint.add(checkBoxClearList);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(panelButtons);

        JButton buttonCommand = new JButton("Zmeniť");
        buttonCommand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    giveAircraftCommands(aircraft, airportData);
                    modifiedAircraft.add(aircraft);
                } catch (RouteNotLoadedException ex) {
                    Logger.getLogger(AircraftControllerCommandsJDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        panelButtons.add(buttonCommand);

        JButton buttonStorno = new JButton("Zrušiť");
        buttonStorno.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AircraftControllerCommandsJDialog.this.dispose();
                radarScreen.setHoveredAircraft(null);
            }
        });
        panelButtons.add(buttonStorno);

        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
        setVisible(true);
    }

    /**
     * Nastaví lietadlu zmemené atribúty ako príkazy, ktoré definoval
     * riadiaci letovej prevádzky cez panel.
     * 
     * @param aircraft Lietadlo, ktorého sa príkazy týkajú.
     * @param airport Letisko, z ktorého sa vyberajú potrebné dáta
     *                nutné pre uskutočnenie niektorých príkazov.
     * @throws RouteNotLoadedException Vyhodená ak sa vybratú trasu cez
     *         panel nepodarí v zozname trás letiska nájsť.
     */
    private void giveAircraftCommands(Aircraft aircraft, Airport airport)
            throws RouteNotLoadedException {
        aircraft.setFinalFlightLevel(sliderFlevel.getValue());
        aircraft.setFinalAirSpeed(sliderSpeed.getValue());
        if (!aircraft.isClearedForDeparture()) {
            aircraft.setClearedForDeparture(checkBoxClearedDeparture.isSelected());
        }
        if (checkBoxChangeRoute.isSelected()) {
            changeAircraftsActualRoute(aircraft);
        }
        if (comboSidRoutePoints.isEnabled()) {
            aircraft.setActualRoute(airport.getRouteByName((String) comboSidRoutePoints.getSelectedItem()));
        }
        if (comboStarRoutePoints.isEnabled()) {
            aircraft.setActualRoute(airport.getRouteByName((String) comboStarRoutePoints.getSelectedItem()));
        }
    }

    /**
     * Ak je vybratá možnosť zmeniť lietadlu celú aktuálnu trasu, táto
     * metóda buď pridá na koniec trasy nové zvolené body alebo celú trasu
     * vymaže a vytvorí novú s len vybratým bodom alebo trasou.
     * 
     * @param aircraft Lietadlo, ktorého sa príkaz týka.
     */
    private void changeAircraftsActualRoute(Aircraft aircraft) {
        String changedWaypoint = comboNextWaypoint.getSelectedItem().toString();
        if (checkBoxClearList.isSelected()) {
            aircraft.getActualRoute().getRoutePoints().clear();
        }
        aircraft.getActualRoute().getRoutePoints().add(changedWaypoint);
    }

}
