
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.Timer;
import net.robobalasko.letisko.siet.AircraftHandler;
import net.robobalasko.letisko.siet.AirportClient;
import net.robobalasko.letiskoserv.data.WaypointNotLoadedException;
import net.robobalasko.letiskoserv.lietadla.Aircraft;
import net.robobalasko.letiskoserv.navigacia.Airport;
import net.robobalasko.letiskoserv.navigacia.Route;
import net.robobalasko.letiskoserv.navigacia.Runway;
import net.robobalasko.letiskoserv.navigacia.VorWaypoint;
import net.robobalasko.letiskoserv.navigacia.Waypoint;

/**
 * Trieda radarovej obrazovky, ktorá vykresľuje aktuálnu situáciu na riadenom letisku.
 *
 * @author rbalasko
 */
public class RadarScreen extends JComponent {

    /**
     * Objekt s nstaveniami zobrazenia na radarovej obrazovke.
     */
    private final RadarScreenOptions screenOptions;

    /**
     * Letiskový handler, ktorý sa stará o aktualizáciu dát obrazovky podľa dát prijatých od servera.
     */
    private final AircraftHandler aircraftHandler;

    /**
     * Zoznam lietadiel, ktoré boli na letisku klientom modifikované.
     */
    private final List<Aircraft> modifiedAircraft;

    /**
     * Letiskový klient, ktorý pre klientskú časť zabezpečuje komunikáciu so serverom.
     */
    private final AirportClient airportClient;

    /**
     * Zoznam lietadiel, ktoré sa aktuálne nachádzajú na servery.
     */
    private List<Aircraft> aircraftList;

    /**
     * Lietadlo, ktoré má riadiaci na letisku vybraté v info panely.
     */
    private Aircraft hoveredAircraft;

    /**
     * Dáta samotného letiska, na ktorom riadenie prebieha.
     */
    private Airport airportData;

    public RadarScreen(final Frame parent, RadarScreenOptions screenOptions,
            AircraftHandler aircraftHandler, AirportClient airportClient, List<Aircraft> modifiedAircraft) {
        this.screenOptions = screenOptions;
        this.aircraftHandler = aircraftHandler;
        this.airportClient = airportClient;
        aircraftList = new LinkedList<Aircraft>();
        this.modifiedAircraft = modifiedAircraft;

        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aircraftList = RadarScreen.this.aircraftHandler.getHandledAircraft();
                if (hoveredAircraft != null) {
                    for (Aircraft aircraft : aircraftList) {
                        if (hoveredAircraft.getCallSign().equals(aircraft.getCallSign())) {
                            hoveredAircraft.setLatitude(aircraft.getLatitude());
                            hoveredAircraft.setLongitude(aircraft.getLongitude());
                            hoveredAircraft.getRouteTrail().clear();
                            hoveredAircraft.getRouteTrail().addAll(aircraft.getRouteTrail());
                        }
                    }
                }
                repaint();
            }
        });
        timer.start();

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                final int mX = e.getX();
                final int mY = e.getY();
                for (Aircraft acft : aircraftList) {
                    // Kontrola či je kurzor nad nejakým lietadlom
                    if (mX >= acft.getLatitude() - 10 && mX <= acft.getLatitude() + 10
                            && mY >= acft.getLongitude() - 10 && mY <= acft.getLongitude() + 10) {
                        acft.setIsSelected(true);
                        RadarScreen.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    } else {
                        acft.setIsSelected(false);
                        RadarScreen.this.setCursor(null);
                    }
                }
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mX = e.getX();
                int mY = e.getY();
                for (Aircraft acft : aircraftList) {
                    // Kontrola či je lietadlo kliknuté
                    if (mX >= acft.getLatitude() - 10 && mX <= acft.getLatitude() + 10
                            && mY >= acft.getLongitude() - 10 && mY <= acft.getLongitude() + 10) {
                        hoveredAircraft = acft;
                        AircraftControllerCommandsJDialog atcCommands
                                = new AircraftControllerCommandsJDialog(parent, acft,
                                        RadarScreen.this.modifiedAircraft, RadarScreen.this, airportData);
                    }
                }
            }
        });
    }

    /**
     * Metóda volaná z metódy {@code paintComponent}, ktorá vykresľuje do radarovej obrazovky všetky letové body, ktoré boli pre letisko načítané.
     *
     * @param g Grafický objekt, na ktorom sa kreslí
     */
    private void paintWaypoints(Graphics g) {
        for (Waypoint wpt : airportData.getWaypoinst()) {
            int guiLongitude = wpt.getPixelCoordX();
            int guiLatitude = wpt.getPixelCoordY();
            g.setColor(Color.WHITE);
            // Ak je bod VOR, vykresľuje sa inak ako obyčajné waypoint-y
            if (wpt instanceof VorWaypoint) {
                g.drawOval(guiLongitude - 5, guiLatitude - 5, 10, 10);
                g.fillOval(guiLongitude - 2, guiLatitude - 2, 4, 4);
                if (screenOptions.isDispPointFreq()) {
                    VorWaypoint vor = (VorWaypoint) wpt;
                    g.setColor(Color.GRAY);
                    g.drawString(String.valueOf(vor.getFrequency()), guiLongitude + 10, guiLatitude);
                }
            } else {
                g.fillRect(guiLongitude - 2, guiLatitude - 2, 4, 4);
            }
            g.setColor(Color.WHITE);
            if (screenOptions.isDispPointNames()) {
                // Názov bodu
                g.drawString(wpt.getName(), guiLongitude + 10, guiLatitude - 10);
            }
            if (screenOptions.isDispPointGps()) {
                // Súradnice bodu
                g.setColor(Color.GRAY);
                g.drawString(wpt.getGpsCoordinates().toString(), guiLongitude + 10, guiLatitude - 25);
            }
        }
    }

    /**
     * Metóda volaná z metódy {@code paintComponent}, ktorá vykresľuje do radarovej obrazovky všetky dráhy, ktoré sa na letisku nachádzajú.
     *
     * @param g Grafický objekt, na ktorom sa kreslí
     */
    private void paintRunways(Graphics g) {
        g.setColor(airportData.isRunwayBlocked() ? Color.RED : Color.GREEN);
        for (Runway rwy : airportData.getRunways()) {
            int startX = rwy.getPixelCoordStartX();
            int startY = rwy.getPixelCoordStartY();
            int endX = rwy.getPixelCoordEndX();
            int endY = rwy.getPixelCoordEndY();
            g.drawLine(startX, startY, endX, endY);
        }
    }

    /**
     * Metóda volaná z metódy {@code paintComponent}, ktorá vykresľuje do radarovej obrazovky diaľkové kruhy.
     *
     * @param g Grafický objekt, na ktorom sa kresli
     */
    private void paintDistanceCircles(Graphics g) {
        int guiLongitude = airportData.getPixelCoordX();
        int guiLatitude = airportData.getPixelCoordY();
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < 30; i++) {
            g.drawOval(guiLongitude - (i * 50), guiLatitude - (i * 50),
                    i * 100, i * 100);
        }
    }

    /**
     * Vytvorí pre lietadlo reťazec jeho aktuálnej a maximálnej rýchlosti, ktorý sa potom pri zapnutí voľby zobrazuje na radare.
     *
     * @param aircraft Lietadlo, pre ktoré sa má raťazec vytvoriť.
     *
     * @return Vytvorený reťazec podľa vybratých volieb nastavenia.
     */
    private String createSpeedString(Aircraft aircraft) {
        String speedString = "";
        if (screenOptions.isDispAircraftActualSpeed()
                || screenOptions.isDispAircraftFinalSpeed()) {
            if (screenOptions.isDispAircraftActualSpeed()) {
                speedString += String.valueOf(aircraft.getActualAirSpeed());
            }
            if (screenOptions.isDispAircraftFinalSpeed()) {
                if (aircraft.getActualAirSpeed() < aircraft.getFinalAirSpeed()) {
                    speedString += " ↑ ";
                } else if (aircraft.getActualAirSpeed() > aircraft.getFinalAirSpeed()) {
                    speedString += " ↓ ";
                } else {
                    speedString += " = ";
                }
                speedString += String.valueOf(aircraft.getFinalAirSpeed());
            }
            speedString += " KTAS";
        }
        return speedString;
    }

    /**
     * Vytvorí pre lietadlo reťazec jeho aktuálnej a maximálnej letovej hladiny, ktorá sa potom pri zapnutí voľby zobrazuje na radare.
     *
     * @param aircraft Lietadlo, pre ktokré sa má reťazec vytvoriť.
     *
     * @return Vytvorený reťazec podľa vybratých volieb nastavenia.
     */
    private String createFlightLevelString(Aircraft aircraft) {
        String flevelString = "";
        if (screenOptions.isDispAircraftActualFlightLevel()
                || screenOptions.isDispAircraftFinalFlightLevel()) {
            flevelString += "FL ";
            if (screenOptions.isDispAircraftActualFlightLevel()) {
                flevelString += String.valueOf(aircraft.getActualFlightLevel());
            }
            if (screenOptions.isDispAircraftFinalFlightLevel()) {
                if (aircraft.getActualFlightLevel() < aircraft.getFinalFlightLevel()) {
                    flevelString += " ↑ ";
                } else if (aircraft.getActualFlightLevel() < aircraft.getFinalFlightLevel()) {
                    flevelString += " ↓ ";
                } else {
                    flevelString = " = ";
                }
                flevelString += String.valueOf(aircraft.getFinalFlightLevel());
            }
        }
        return flevelString;
    }

    /**
     * Metóda volaná z metódy {@code paintComponent}, ktorá vykresľuje do radarovej obrazovky diaľkové kruhy.
     *
     * @param g Grafický objekt, na ktorom kreslí
     */
    private void paintAircraft(Graphics g) {
        if (!aircraftList.isEmpty()) {
            for (Aircraft acft : aircraftList) {
                // Vykreslenie modrej bezpečnej zóny okolo lietadla
                Color safeZoneColor;
                if (acft.isClearedForDeparture()) {
                    if (acft.isGoingAround()) {
                        safeZoneColor = Color.ORANGE;
                    } else {
                        safeZoneColor = Color.BLUE;
                    }
                } else {
                    safeZoneColor = Color.PINK;
                }
                g.setColor(acft.isClearedForDeparture() ? Color.BLUE : Color.PINK);
                int x = (int) acft.getLatitude();
                int y = (int) acft.getLongitude();
                g.drawOval(x - 15, y - 15, 30, 30);
                paintAircraftSeparationError(g, acft, x, y);
                // Vykreslenie štvorca označujúceho lietadlo
                g.setColor(Color.CYAN);
                g.drawRect(x - 4, y - 4, 8, 8);
                g.drawRect(x - 1, y - 1, 2, 2);
                g.setColor(Color.GREEN);
                // Smerovú šípku lietadla stačí vykresliť len ak sa už hýbe
                if (acft.getActualAirSpeed() > 100) {
                    g.drawLine(x, y, acft.getDirectionLine().x, acft.getDirectionLine().y);
                }
                paintAircraftFlightInfo(g, acft, x, y);
            }
        }
    }

    /**
     * Pre všetky lietadlá, ktorá sa nachádzajú na letisku vykresľuje červenú zónu okolo lietadla, v ktorého menšej ako minimálnej blízkosti sa nachádza nejaké iné lietadlo.
     *
     * @param g Grafický objekt, na ktorý sa kreslí.
     * @param aircraft Lietadlo, ktorého narušenie bezpečnej vzdialenosti sa má vykresliť.
     * @param x Horizontálna pozícia lietadla.
     * @param y Vertikálna pozícia lietadla.
     */
    private void paintAircraftSeparationError(Graphics g, Aircraft aircraft, int x, int y) {
        for (Aircraft a : aircraftList) {
            if (!a.equals(aircraft)) {
                if (Math.abs(a.getLatitude() - x) < 15
                        && Math.abs(a.getLongitude() - y) < 15
                        && Math.abs(a.getActualFlightLevel() - aircraft.getActualFlightLevel()) < 20) {
                    g.setColor(Color.RED);
                    g.drawOval(x - 15, y - 15, 30, 30);
                    g.drawOval((int) a.getLatitude() - 15, (int) a.getLongitude() - 15, 30, 30);
                }
            }
        }
    }

    /**
     * Vykreslí dodatočné dáta o lietadle a stave letu: volací znak, aktuálna & max výska a aktuálna a max hladina letu.
     *
     * @param g Grafický objekt, na ktorom sa kreslí.
     * @param aircraft Lietadlo, ku ktorému sa majú dáta vykresliť.
     * @param x Horizontálna pozícia lietadla.
     * @param y Vertikálna pozícia lietadla.
     */
    private void paintAircraftFlightInfo(Graphics g, Aircraft aircraft, int x, int y) {
        if (aircraft.isAircraftLanding()) {
            g.setColor(Color.PINK);
        } else if (aircraft.isGoingAround()) {
            g.setColor(Color.ORANGE);
        } else {
            g.setColor(Color.CYAN);
        }
        g.drawString(aircraft.getCallSign(), x + 10, y - 22);
        if (screenOptions.isDispAircraftType()) {
            g.drawString(aircraft.getAircraftType().toString(), x + 10, y - 9);
        }
        g.setColor(Color.YELLOW);
        if (screenOptions.isDispAircraftActualFlightLevel()) {
            g.drawString(String.valueOf(createFlightLevelString(aircraft)), x + 10, y + 20);
        }
        if (screenOptions.isDispAircraftActualSpeed()
                || screenOptions.isDispAircraftFinalSpeed()) {
            g.drawString(createSpeedString(aircraft), x + 10, y + 5);
        }
    }

    /**
     * Vykreslí cestu, ktorá ešte zostáva odletieť aktuálne zvolenému lietadlu v riadenej oblasti.
     *
     * @param g Grafický objekt, na ktorý sa kreslí.
     */
    private void paintRoute(Graphics g) throws WaypointNotLoadedException {
        Route rte = hoveredAircraft.getActualRoute();
        g.setColor(Color.MAGENTA);
        Waypoint fWpt = airportData.getWaypointByName((String) rte.getRoutePoints().get(0));
        int acSX = (int) hoveredAircraft.getLatitude();
        int acSY = (int) hoveredAircraft.getLongitude();
        int fwEX = fWpt.getPixelCoordX();
        int fwEY = fWpt.getPixelCoordY();
        g.drawLine(acSX, acSY, fwEX, fwEY);
        for (int i = 0; i < rte.getRoutePoints().size() - 1; i++) {
            Waypoint wptS = airportData.getWaypointByName((String) rte.getRoutePoints().get(i));
            Waypoint wptE = airportData.getWaypointByName((String) rte.getRoutePoints().get(i + 1));
            int wptSX = wptS.getPixelCoordX();
            int wptSY = wptS.getPixelCoordY();
            int wptEX = wptE.getPixelCoordX();
            int wptEY = wptE.getPixelCoordY();
            g.drawLine(wptSX, wptSY, wptEX, wptEY);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (airportData != null) {
            if (screenOptions.isDispDistCircles()) {
                paintDistanceCircles(g);
            }
            g.setColor(Color.ORANGE);
            g.drawOval(airportData.getPixelCoordX() - 50, airportData.getPixelCoordY() - 50, 100, 100);
            paintRunways(g);
            paintWaypoints(g);
            paintAircraft(g);
            if (hoveredAircraft != null) {
                try {
                    paintRoute(g);
                    g.setColor(Color.RED);
                    for (Point point : hoveredAircraft.getRouteTrail()) {
                        g.fillRect((int) point.getX(), (int) point.getY(), 2, 2);
                    }
                } catch (WaypointNotLoadedException ex) {
                    Logger.getLogger(RadarScreen.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            g.setColor(Color.YELLOW);
            if (airportData == null) {
                g.drawString("Načítavam dáta letiska...", 100, 100);
            }
        }
    }

    public void setAirportData(Airport airportData) {
        this.airportData = airportData;
    }

    public void setHoveredAircraft(Aircraft hoveredAircraft) {
        this.hoveredAircraft = hoveredAircraft;
    }

}
