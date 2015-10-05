package net.robobalasko.letiskoserv.data;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.robobalasko.letiskoserv.guihelper.RadarScreenDataCalculator;
import net.robobalasko.letiskoserv.navigacia.Airport;
import net.robobalasko.letiskoserv.navigacia.AirportRouteTypesEnum;
import net.robobalasko.letiskoserv.navigacia.InvalidGPSCoordinatesException;
import net.robobalasko.letiskoserv.navigacia.InvalidRouteDataException;
import net.robobalasko.letiskoserv.navigacia.InvalidRunwayDataException;
import net.robobalasko.letiskoserv.navigacia.InvalidWaypointDataException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Trieda, ktorá zo zadanej zložky načíta všetky letiská,
 * ktoré sa vo forme xml súborov v danej zložke nachádzajú.
 *
 * @author rbalasko
 */
public class AirportLoader {

    /**
     * Zdrojová zložka, v ktorej sa nachádzajú súbory s letiskami.
     */
    private final File sourceFolder;

    /**
     * Základný konštruktor nastavuje adresu atribútu  {@code xmlDir},
     * kde sa budú hľadať xml súbory letísk.
     *
     * @param xmlDir Cesta ku zložke s letiskami
     */
    public AirportLoader(File xmlDir) {
        sourceFolder = xmlDir;
    }

    /**
     * Prehľadá zadanú zložku so súbormi a vyberie z nej
     * všetky súbory, ktoré majú príponu .xml
     *
     * @return Zoznam názvov nájdených súborov
     */
    public List<String> listAvailAirports() {
        List<String> availAirports = new LinkedList<String>();
        File[] files = sourceFolder.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().contains(".xml")) {
                String airportName = file.getName();
                airportName = airportName.substring(0, airportName.lastIndexOf('.'));
                availAirports.add(airportName);
            }
        }
        return availAirports;
    }

    /**
     * Načíta letisko z xml súboru podľa zadaného názvu v parametre
     *
     * Pri zadaní {@code icaoCode} ako {@code lzib} bude metóda hľadať v zložke súbor {@code lzib.xml}
     *
     * @param icaoCode Kód letiska, ktorého xml súbor sa má načítať
     * @param radarScreenSize Pole obsahujúce výšku a šírku zobrazenia radaru.
     *
     * @return Objekt typu {@code Airport} obsahujúci všetky informácie načítané zo súboru
     *
     * @throws InvalidRunwayDataException Vyhodená, ak už práve pridávaná dráha na letisku existuje.
     * @throws InvalidRouteDataException Vyhodená, ak už práve pridávaná letová trasa na letisku existuje.
     * @throws InvalidWaypointDataException Vyhodená, ak už práve pridávaný letový body na letisku existuje.
     */
    public Airport loadAirport(String icaoCode, int[] radarScreenSize)
            throws InvalidRunwayDataException, InvalidRouteDataException, InvalidWaypointDataException {
        Airport loadedAirport = new Airport();
        try {
            // Načítanie xml súboru letiska
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(sourceFolder + "/" + icaoCode + ".xml");
            doc.getDocumentElement().normalize();

            // Vyparsovanie všetkých potrebných dát zo súboru
            parseAirportRoot(doc, loadedAirport);
            parseGPSData(doc, loadedAirport);
            parseAirportAreaBounds(doc, loadedAirport);
            parseAirportRunways(doc, loadedAirport);
            parseAirportWaypoins(doc, loadedAirport);
            parseAirportSidRoutes(doc, loadedAirport);
            parseAirportStarRoutes(doc, loadedAirport);
        } catch (SAXException ex) {
            Logger.getLogger(AirportLoader.class.getName()).log(Level.SEVERE,
                    "Unexpected parser error.", ex);
        } catch (IOException ex) {
            Logger.getLogger(AirportLoader.class.getName()).log(Level.SEVERE,
                    "The file containing the airport data could not be loaded.", ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(AirportLoader.class.getName()).log(Level.SEVERE,
                    "Parser configuration error.", ex);
        } catch (InvalidGPSCoordinatesException ex) {
            Logger.getLogger(AirportLoader.class.getName()).log(Level.SEVERE,
                    "The GPS coordinates that you tried to set were not valid.", ex);
        }
        RadarScreenDataCalculator rsdc
                = new RadarScreenDataCalculator(loadedAirport, radarScreenSize[0], radarScreenSize[1]);
        rsdc.calculateAirportGUIData();
        return loadedAirport;
    }
    
    /**
     * Vyparsovanie koreňového elementu <airport> z .xml súboru.
     * 
     * @param doc Dokument, z ktorého sa dáta parsujú.
     * @param loadedAirport Objekt vygenerovaného letiska, ktorý sa z dát vytvára.
     */
    private void parseAirportRoot(Document doc, Airport loadedAirport) {
        // Nacitanie korenoveho elementu <airport>
        Element airport = (Element) doc.getElementsByTagName("airport").item(0);
        loadedAirport.setIcaoCode(airport.getAttribute("icao"));
        // Nacitanie elementu <name> s nazvom letiska
        Element airportName = (Element) doc.getElementsByTagName("name").item(0);
        loadedAirport.setAirportName(airportName.getTextContent());
    }
    
    /**
     * Vyparsovanie GPS dát letiska z .xml súboru.
     * 
     * @param doc Dokument, z ktorého sa dáta parsujú.
     * @param loadedAirport Objekt vygenerovaného letiska, ktorý sa z dát vytvára.
     * 
     * @throws InvalidGPSCoordinatesException Vyhodená GPS dáta letiska nemajú
     *         v .xml súbore definované správne hodnoty a systém sa ich pokúsy nastaviť.
     */
    private void parseGPSData(Document doc, Airport loadedAirport)
            throws InvalidGPSCoordinatesException {
        // Nacitanie elementu <gps> so suradnicami letiska
        Element airportGpsCoordinates = (Element) doc.getElementsByTagName("gps").item(0);
        double airportLat = Double.parseDouble(airportGpsCoordinates.getAttribute("lat"));
        double airportLong = Double.parseDouble(airportGpsCoordinates.getAttribute("long"));
        loadedAirport.setGpsCoordinates(airportLat, airportLong);
    }
    
    /**
     * Vyparsovanie GPS dát hraníc riadenej oblasti z .xml súboru.
     * 
     * @param doc Dokument, z ktorého sa dáta parsujú.
     * @param loadedAirport Objekt vygenerovaného letiska, ktorý sa z dát vytvára.
     */
    private void parseAirportAreaBounds(Document doc, Airport loadedAirport) {
        // Nacitanie elementu <area> s hranicami riadenej oblasti
        Element airportAreaBorders = (Element) doc.getElementsByTagName("area").item(0);
        double areaTopBorder = Double.parseDouble(airportAreaBorders.getAttribute("top"));
        double areaRightBorder = Double.parseDouble(airportAreaBorders.getAttribute("right"));
        double areaBottomBorder = Double.parseDouble(airportAreaBorders.getAttribute("bottom"));
        double areaLeftBorder = Double.parseDouble(airportAreaBorders.getAttribute("left"));
        loadedAirport.setAreaTopBorder(areaTopBorder);
        loadedAirport.setAreaRightBorder(areaRightBorder);
        loadedAirport.setAreaBottomBorder(areaBottomBorder);
        loadedAirport.setAreaLeftBorder(areaLeftBorder);
    }
    
    /**
     * Vyparsovanie dát letiskových dráh z .xml súboru.
     * 
     * @param doc Dokument, z ktorého sa dáta parsujú.
     * @param loadedAirport Objekt vygenerovaného letiska, ktorý sa z dát vytvára.
     * 
     * @throws InvalidRunwayDataException Vyhodená, ak aktuálne pridávaná dráha
     *         už v zozname dráh letiska existuje.
     */
    private void parseAirportRunways(Document doc, Airport loadedAirport)
            throws InvalidRunwayDataException {
        // Nacitanie drah letiska <runway>
        NodeList runways = doc.getElementsByTagName("runway");
        for (int i = 0; i < runways.getLength(); i++) {
            Element runway = (Element) runways.item(i);
            int runwayNumber = Integer.parseInt(runway.getAttribute("id"));
            int runwayLength = Integer.parseInt(runway.getAttribute("length"));
            double runwayLat = Double.parseDouble(runway.getAttribute("lat"));
            double runwayLon = Double.parseDouble(runway.getAttribute("long"));
            loadedAirport.setRunway(runwayNumber, runwayLength, runwayLat, runwayLon);
        }
    }
    
    /**
     * Vyparsovanie letových bodov z .xml súboru.
     * 
     * @param doc Dokument, z ktorého sa dáta parsujú.
     * @param loadedAirport Objekt vygenerovaného letiska, ktorý sa z dát vytvára.
     * 
     * @throws InvalidWaypointDataException Vyhodená, ak aktuálne pridávaný bod
     *         už v zozname bodov letiska existuje.
     */
    private void parseAirportWaypoins(Document doc, Airport loadedAirport)
            throws InvalidWaypointDataException {
        // Nacitanie cestovnych bodov na letisku <waypoint>
        NodeList waypoints = doc.getElementsByTagName("waypoint");
        for (int i = 0; i < waypoints.getLength(); i++) {
            Element waypoint = (Element) waypoints.item(i);
            String name = waypoint.getAttribute("name");
            double latitude = Double.parseDouble(waypoint.getAttribute("lat"));
            double longitude = Double.parseDouble(waypoint.getAttribute("long"));
            double frequency = 0;
            if (waypoint.hasAttribute("freq")) {
                frequency = Double.parseDouble(waypoint.getAttribute("freq"));
                loadedAirport.setWaypoint(name, latitude, longitude, frequency);
            } else {
                loadedAirport.setWaypoint(name, latitude, longitude);
            }
        }
    }
    
    /**
     * Vyparsovanie odletových trás letiska z .xml súboru.
     * 
     * @param doc Dokument, z ktorého sa dáta parsujú.
     * @param loadedAirport Objekt vygenerovaného letiska, ktorý sa z dát vytvára.
     * 
     * @throws InvalidRouteDataException Vyhodená, ak aktuálne pridávaná trasa
     *         už v zozname trás letiska existuje.
     */
    private void parseAirportSidRoutes(Document doc, Airport loadedAirport)
            throws InvalidRouteDataException {
        // Nacitanie odletovych tras z letiska <sids>
        NodeList sidsList = doc.getElementsByTagName("sids");
        for (int i = 0; i < sidsList.getLength(); i++) {
            Element sidList = (Element) sidsList.item(i);
            int runwayNumber = Integer.parseInt(sidList.getAttribute("id"));
            NodeList sids = sidList.getChildNodes();
            for (int j = 0; j < sids.getLength(); j++) {
                if (sids.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    Element sid = (Element) sids.item(j);
                    String routeName = sid.getAttribute("id");
                    String[] routeWaypoints = sid.getAttribute("route").split(",");
                    loadedAirport.setRoute(runwayNumber, routeName, routeWaypoints, AirportRouteTypesEnum.SID);
                }
            }
        }
    }
    
    /**
     * Vyparsovanie odletových trás z letiska z .xml súboru.
     * 
     * @param doc Dokument, z ktorého sa dáta parsujú.
     * @param loadedAirport Objekt vygenerovaného letiska, ktorý sa z dát vytvára.
     * 
     * @throws InvalidRouteDataException Vyhodená, ak aktuálne pridávaná trasa
     *         užu v zozname trás letiska existuje.
     */
    private void parseAirportStarRoutes(Document doc, Airport loadedAirport)
            throws InvalidRouteDataException {
        // Nacitanie priletovych tras na letisko <stars>
        NodeList starsList = doc.getElementsByTagName("stars");
        for (int i = 0; i < starsList.getLength(); i++) {
            Element starList = (Element) starsList.item(i);
            int runwayNumber = Integer.parseInt(starList.getAttribute("id"));
            NodeList stars = starList.getChildNodes();
            for (int j = 0; j < stars.getLength(); j++) {
                if (stars.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    Element sid = (Element) stars.item(j);
                    String routeName = sid.getAttribute("id");
                    String[] routeWaypoints = sid.getAttribute("route").split(",");
                    loadedAirport.setRoute(runwayNumber, routeName, routeWaypoints, AirportRouteTypesEnum.STAR);
                }
            }
        }
    }

}
