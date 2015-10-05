package net.robobalasko.letiskoserv.navigacia;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.robobalasko.letiskoserv.data.WaypointNotLoadedException;

/**
 * Trieda opisujúca načítané letisko z .xml súborov.
 * 
 * Podľa dát načítaných do objektu {@code Airport} server počíta
 * navigáciu pre lietadlá, ktoré letia vo vzdušnom priestore samé
 * a taktiež radarová obrazovka v klientskej časti vykresľuje potrebné
 * dráhy a navigačné body pre možnosť riadenia prevádzky na letisku.
 * 
 * @author rbalasko
 */
public class Airport implements Serializable {

    /**
     * Oficiálny názov letiska podľa ICAO.
     */
    private String airportName;

    /**
     * Štvorpísmenný kód letiska podľa ICAO.
     */
    private String icaoCode;

    /**
     * Gps súradnice letiska.
     */
    private GPSCoordinates gpsCoordinates;

    /**
     * Gps koordinát označujúci koniec riadenej oblasti z vrchnej strany.
     * Používa sa pre výpočet umiestnenia objektov letiska na vytvorené plátno radarovej obrazovky.
     */
    private double areaTopBorder;

    /**
     * Gps koordinát označujúci koniec riadenej oblasti z pravej strany.
     * Používa sa pre výpočet umiestnenia objektov letiska na vytvorené plátno radarovej obrazovky.
     */
    private double areaRightBorder;

    /**
     * Gps koordinát označujúci koniec riadenej oblasti zo spodnej strany.
     * Používa sa pre výpočet umiestnenia objektov letiska na vytvorené plátno radarovej obrazovky.
     */
    private double areaBottomBorder;

    /**
     * Gps koordinát označujúci koniec riadenej oblasti z ľavej strany.
     * Používa sa pre výpočet umiestnenia objektov letiska na vytvorené plátno radarovej obrazovky.
     */
    private double areaLeftBorder;

    /**
     * Zoznam dráh, ktoré sa na letisku nachádzajú.
     */
    private final List<Runway> runways;

    /**
     * Zoznam odletových a príletových trás,
     * ktorá sa na letisku nachádzajú.
     */
    private final List<Route> routes;

    /**
     * Zoznam navigačných bodov, ktoré sa na letisku nachádzajú.
     */
    private final List<Waypoint> waypoints;

    /**
     * Horizontálna pozícia letiska na radarovej obrazovke.
     */
    private int pixelCoordX;

    /**
     * Vertikálna pozícia letiska na radarovej obrazovke.
     */
    private int pixelCoordY;

    /**
     * Celková šírka zobrazenia na radarovej obrazovke.
     */
    private int areaPixelWidth;

    /**
     * Celková výška zobrazenia na radarovej obrazovke.
     */
    private int areaPixelHeight;
    
    /**
     * Definuje, či je aktuálne dráha na letisku voľná.
     */
    private boolean runwayBlocked;
    
    /**
     * ICAO identifikátor lietadla, ktoré aktuálne blokuje dráhu.
     */
    private String aircraftBlockingRunway;

    /**
     * Základný konštruktor objektu {@code Airport} len nastavuje
     * atribúty so spájanými zoznamami, keďže ostatné atribúty nastavuje
     * priamo objekt, ktorý číta dáta letiska z XML súboru.
     */
    public Airport() {
        runways = new LinkedList<Runway>();
        routes = new LinkedList<Route>();
        waypoints = new LinkedList<Waypoint>();
        runwayBlocked = false;
        aircraftBlockingRunway = "";
    }

    /**
     * Získa objekt trasového bodu podľa zadného názvu.
     *
     * @param name Meno trasového bodu, ktorý sa má získať.
     *
     * @return Trasový bod ako {@code Waypoint} objekt.
     * 
     * @throws net.robobalasko.letiskoserv.data.WaypointNotLoadedException
     *              Ak sa bod podľa názvu zadaného v parametri nepodarí nájsť.
     */
    public Waypoint getWaypointByName(String name)
            throws WaypointNotLoadedException {
        for (Waypoint wpt : waypoints) {
            if (wpt.getName().equals(name)) {
                return wpt;
            }
        }
        throw new WaypointNotLoadedException();
    }

    /**
     * Získa objekt trasy podľa zadaného názvu.
     * 
     * @param name Názov letovej trasy, ktorá sa má získať.
     * 
     * @return Letová trasa ako {@code Route} objekt.
     * 
     * @throws net.robobalasko.letiskoserv.navigacia.RouteNotLoadedException
     *              Ak sa trasu podľa názvu zadaného v parametri nepodarí nájsť.
     */
    public Route getRouteByName(String name)
            throws RouteNotLoadedException {
        for (Route rte : routes) {
            if (rte.getRouteName().equals(name)) {
                return rte;
            }
        }
        throw new RouteNotLoadedException();
    }

    /**
     * Vráti náhodne vybratý letová bod zo zoznamu bodov,
     * ktoré patria k letisku s tým, že vynechá bod, ktorý je špecifikovaný
     * ako parameter.
     * 
     * Ak je ako názov letového bodu zadaný parameter null, vygeneruje
     * sa náhodný bod zo všetkých bodov (žiadny sa nevynechá).
     * 
     * @param excludePoint Názov bodu, ktorý sa má vynechať.
     * 
     * @return Náhodne vybratý bod ako {@code Waypoint} objekt.
     */
    public Waypoint getRandomWaypoint(String excludePoint) {
        int rand = (new Random()).nextInt(waypoints.size());
        if (excludePoint == null) {
            return waypoints.get(rand);
        }
        Waypoint wpt;
        while ((wpt = waypoints.get(rand)).getName().equals(excludePoint)) {
            rand = (new Random()).nextInt(waypoints.size());
        }
        return wpt;
    }

    /**
     * Vráti zoznam trás nachádzajúcich sa na letisku podľa toho,
     * aký typ trasy je v parametry zadaný.
     * 
     * @param routeType Typ trás, ktorých zoznam sa má vrátiť.
     * 
     * @return Spájaný zoznam trás podľa špecifikovaného typu trasy.
     */
    public LinkedList getSpecificRoutes(AirportRouteTypesEnum routeType) {
        LinkedList<String> sidRoutes = new LinkedList<String>();
        for (Route rte : routes) {
            if (rte.getRouteType() == routeType) {
                sidRoutes.add(rte.getRouteName());
            }
        }
        return sidRoutes;
    }

    /**
     * Vráti textovú reprezentáciu objektu letiska
     * vo formáte, napr. LZIB - Letisko M. R. Štefánika, Bratislava
     * 
     * @return Textová reprezentácia letiska.
     */
    @Override
    public String toString() {
        return icaoCode.toUpperCase() + " - " + airportName;
    }

    /**
     * Vráti celý názov letiska definovaný podľa ICAO.
     * @return Reťazec s názvom letiska
     */
    public String getAirportName() {
        return airportName;
    }

    /**
     * Nastavuje letisku názov.
     *
     * @param airportName Názov, ktorý sa má nastaviť
     */
    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    /**
     * Nastavuje GPS súradnice pre letisko.
     *
     * @param latitude Hodnota zemepisnej šírky.
     * @param longitude Hodnota zemepisnej dĺžky.
     *
     * @throws InvalidGPSCoordinatesException Podmienka je vyhodená ak sú nastavované údaje mimo rozmedzia pre GPS súradnice.
     */
    public void setGpsCoordinates(double latitude, double longitude) throws InvalidGPSCoordinatesException {
        this.gpsCoordinates = new GPSCoordinates(latitude, longitude);
    }

    /**
     * Vracia GPS súradnice letiska.
     *
     * @return Súradnice vo forme {@code GPSCoordinates} objektu.
     */
    public GPSCoordinates getGpsCoordinates() {
        return gpsCoordinates;
    }

    /**
     * Nastavuje GPS súradnicu hornej hranice riadenej oblasti.
     *
     * @param areaTopBorder GPS súradnica hornej hranice.
     */
    public void setAreaTopBorder(double areaTopBorder) {
        this.areaTopBorder = areaTopBorder;
    }

    /**
     * Vracia GPS súranicu hornej hranice riadnej oblasti.
     * 
     * @return Desatinná hodnota zemepisnej dĺžky horného okraja riadenej oblasti.
     */
    public double getAreaTopBorder() {
        return areaTopBorder;
    }

    /**
     * Nastavuje GPS súradnicu pravej hranice riadenej oblasti.
     *
     * @param areaRightBorder GPS súradnica pravej hranice
     */
    public void setAreaRightBorder(double areaRightBorder) {
        this.areaRightBorder = areaRightBorder;
    }

    /**
     * Vracia GPS súradnicu pravej hranice riadenej oblasti.
     * 
     * @return Desatinná hodnota zemepisnej šírky pravého okraja riadenej oblasti.
     */
    public double getAreaRightBorder() {
        return areaRightBorder;
    }

    /**
     * Vracia GPS súradnicu spodnej hranice riadenej oblasti.
     * 
     * @return Desatinná hodnota zemepisnej výšky spodného okraja riadenej oblasti. 
     */
    public double getAreaBottomBorder() {
        return areaBottomBorder;
    }

    /**
     * Nastavuje GPS súradnicu spodnej hranice riadenej oblasti.
     * 
     * @param areaBottomBorder GPS súradnica spodnej hranice.
     */
    public void setAreaBottomBorder(double areaBottomBorder) {
        this.areaBottomBorder = areaBottomBorder;
    }

    /**
     * Vracia GPS súradnicu ľavej hranice riadenej oblasti.
     * 
     * @return Desatinná hodnota zemepisnej šírky ľavého okraja riadenej oblasti.
     */
    public double getAreaLeftBorder() {
        return areaLeftBorder;
    }

    /**
     * Nastavuje GPS súradnicu ľavej hranice riadenej oblasti.
     * 
     * @param areaLeftBorder GPS súradnica ľavej hranice.
     */
    public void setAreaLeftBorder(double areaLeftBorder) {
        this.areaLeftBorder = areaLeftBorder;
    }

    /**
     * Vráti štvorpísmenný kód letiska podľa ICAO.
     * 
     * @return Reťazec s ICAO kódom letiska.
     */
    public String getIcaoCode() {
        return icaoCode;
    }

    /**
     * Nastavuje letisku ICAO kód.
     *
     * @param icaoCode ICAO kód, ktorý sa má nastaviť.
     */
    public void setIcaoCode(String icaoCode) {
        this.icaoCode = icaoCode;
    }

    /**
     * Vráti zoznam všetkých dráh letiska.
     *
     * @return Spájaný zoznam objektov {@code Runway}.
     */
    public List<Runway> getRunways() {
        return runways;
    }

    /**
     * Pridá do zoznamu s dráhami novú dráhu,
     * kontroluje aj či už náhodou aktuálne pridávaná dráha v zozname dráh existuje.
     *
     * @param runwayNumber Číslo (kurz) pridávanej dráhy.
     * @param runwayLength Dĺžka pridávanej dráhy.
     * @param runwayLat Zemepisná stredového bodu pridávanej dráhy.
     * @param runwayLon Zemepsiná dĺžka stredového bodu pridávanej dráhy.
     * 
     * @throws InvalidRunwayDataException Vyhodená ak už práve pridávaná dráha v zozname dráh existuje
     */
    public void setRunway(int runwayNumber, int runwayLength,
            double runwayLat, double runwayLon) throws InvalidRunwayDataException {
        if (checkRunwayExistence(runwayNumber)) {
            throw new InvalidRunwayDataException();
        }
        this.runways.add(new Runway(runwayNumber, runwayLength, runwayLat, runwayLon));
    }

    /**
     * Kontroluje, či sa práve zadávaná dráha už náhodou nenachádza v zozname pridaných dráh.
     *
     * @param runwayNumber Číslo dráhy, ktorá sa má skontrolovať.
     *
     * @return True ak dráha neexistuje, False ak áno.
     */
    private boolean checkRunwayExistence(int runwayNumber) {
        boolean runwayExists = false;
        for (Runway rwy : runways) {
            if (rwy.getRunwayNumber() == runwayNumber) {
                runwayExists = true;
                break;
            }
        }

        return runwayExists;
    }

    /**
     * Získa zoznam všetkých trás, ktorá vedú od / do letiska.
     *
     * @return Spájaný zoznam objektov {@code Route}.
     */
    public List<Route> getRoutes() {
        return routes;
    }

    /**
     * pridá novú trasu do zoznamu trás, kontroluje aj, či práve pridávaná dráha v zoznam neexistuje.
     *
     * @param runwayNumber Číslo dráhy, ku ktorej pridávaná trasa patrí.
     * @param routeName Identifikačný názov pridávanej trasy.
     * @param routeWaypoints Zoznam cestovných bodov, cez ktoré trasa vedie.
     * @param routeType Typ trasy.
     *
     * @throws InvalidRouteDataException Vyhodená ak už zadávaná trasa v zozname existuje
     */
    public void setRoute(int runwayNumber, String routeName,
            String[] routeWaypoints, AirportRouteTypesEnum routeType) throws InvalidRouteDataException {
        if (checkRouteExistence(routeName)) {
            throw new InvalidRouteDataException();
        }
        List<String> rteWpts = new LinkedList<String>();
        rteWpts.addAll(Arrays.asList(routeWaypoints));
        routes.add(new Route(runwayNumber, routeName, routeType, rteWpts));
    }

    /**
     * Kontroluje, či sa zadaná dráha už náhodou nenachádza v zozanem dráh, ktoré patria k letisku.
     *
     * @param routeName Názov dráhy, ktorá sa má skontrolovať.
     *
     * @return True ak dráha existuje, False inak.
     */
    private boolean checkRouteExistence(String routeName) {
        boolean routeExists = false;
        for (Route route : routes) {
            if (route.getRouteName().equals(routeName)) {
                routeExists = true;
                break;
            }
        }

        return routeExists;
    }

    /**
     * Získa zoznam všetkých trasových bodov letiska.
     *
     * @return Spájaný zoznam objektov {@code Waypoint}
     */
    public List<Waypoint> getWaypoinst() {
        return waypoints;
    }

    /**
     * Pridá do zoznamov bodov na letisku nový bod, kontroluje aj, či sa už zadaný bod v zozname nachádza.
     *
     * @param name Názov bodu, ktorý sa má pridať.
     * @param latitude Zemepisná číska pridávaného bodu.
     * @param longitude Zemepisná dĺžka pridávaného bodu.
     *
     * @throws InvalidWaypointDataException Vyhodená ak sa zadávaný bod už nachádza v zozname letových bodov letiska.
     */
    public void setWaypoint(String name, double latitude, double longitude) throws InvalidWaypointDataException {
        if (checkWaypointExistence(name)) {
            throw new InvalidWaypointDataException();
        }
        this.waypoints.add(new Waypoint(name, latitude, longitude));
    }

    /**
     * Pridá do zoznam bodov na letisku nový bod typu VOR,
     * kontroluje aj, či sa už zadaný bod v zozname nachádza
     *
     * @param name Názov bodu, ktorý sa má pridať.
     * @param latitude Zemepisná číska pridávaného bodu.
     * @param longitude Zemepisná dĺžka pridávaného bodu.
     * @param frequency Frekvencia bodu v Mhz.
     *
     * @throws InvalidWaypointDataException Vyhodená ak sa zadávaný bod už nachádza v zozname letových bodov letiska.
     */
    public void setWaypoint(String name, double latitude, double longitude, double frequency) throws InvalidWaypointDataException {
        if (checkRouteExistence(name)) {
            throw new InvalidWaypointDataException();
        }
        try {
            this.waypoints.add(new VorWaypoint(name, latitude, longitude, frequency));
        } catch (InvalidFrequencyException ex) {
            Logger.getLogger(Airport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Kontroluje, či sa zadaný trasový bod už náhodou nenachádza v zoznam trasových bodov letiska.
     *
     * @param waypointName Názov bodu, ktorý sa má skontrolovať.
     *
     * @return True ak sa bod nachádza v zoznam, False inak.
     */
    private boolean checkWaypointExistence(String waypointName) {
        boolean waypointExists = false;
        for (Waypoint wpt : waypoints) {
            if (wpt.getName().equals(waypointName)) {
                waypointExists = true;
                break;
            }
        }

        return waypointExists;
    }

    /**
     * Vráti hodnotu zemepisnej šírky letiska v pixeloch.
     * 
     * @return Celočíselná hodnota zemepisnej šírky prepočítaná do pixelov.
     */
    public int getPixelCoordX() {
        return pixelCoordX;
    }

    /**
     * Nastavuje hodnotu zemepisnej šírky letiska v pixeloch.
     * 
     * @param pixelCoordX Celočíselná hodnota zemepisnej šírky prepočítaná do pixelov.
     */
    public void setPixelCoordX(int pixelCoordX) {
        this.pixelCoordX = pixelCoordX;
    }

    /**
     * Vráti hodnotu zemepisnej výšky letiska v pixeloch.
     * 
     * @return Celočíselná hodnota zemepisnej šírky prepočítaná do pixelov.
     */
    public int getPixelCoordY() {
        return pixelCoordY;
    }

    /**
     * Nastavuje hodnotu zemepisnej výšky v pixeloch.
     * 
     * @param pixelCoordY Celočíselná hodnota zemepisnej šírky prepočítaná do pixelov.
     */
    public void setPixelCoordY(int pixelCoordY) {
        this.pixelCoordY = pixelCoordY;
    }

    /**
     * Vráti celkovú šírku riadenej oblasti letiska v pixeloch.
     * 
     * @return Celočíselný údaj šírky riadenej oblasti v pixeloch.
     */
    public int getAreaPixelWidth() {
        return areaPixelWidth;
    }

    /**
     * Nastavuje celkovú šírku riadenej oblasti letiska v pixeloch.
     * 
     * @param areaPixelWidth Celočíselný údaj riadenej oblasti v pixeloch.
     */
    public void setAreaPixelWidth(int areaPixelWidth) {
        this.areaPixelWidth = areaPixelWidth;
    }

    /**
     * Vráti celkovú výšku riadenej oblasti letiska v pixeloch.
     * 
     * @return Celočíselný údaj výšky riadenej oblasti v pixeloch.
     */
    public int getAreaPixelHeight() {
        return areaPixelHeight;
    }

    /**
     * Nastavuje celkovú šírku riadenej oblasti letiska v pixeloch.
     * 
     * @param areaPixelHeight Celočíselný údaj riadenej oblasti v pixeloch.
     */
    public void setAreaPixelHeight(int areaPixelHeight) {
        this.areaPixelHeight = areaPixelHeight;
    }

    /**
     * Vrati hodnotu, či je dráha blokovaná alebo nie
     * 
     * @return True, ak je dráha blokovaná.
     */
    public boolean isRunwayBlocked() {
        return runwayBlocked;
    }

    /**
     * Nastavuje hodnotu, či je dráha blokovaná.
     * 
     * @param runwayBlocket Hodnota, či je dráha blokovaná.
     */
    public void setRunwayBlocked(boolean runwayBlocket) {
        this.runwayBlocked = runwayBlocket;
    }

    /**
     * Vráti volací znak lietadla, ktoré blokuje dráhu.
     * 
     * @return Reťazec s volacím znakom lietadla.
     */
    public String getAircraftBlockingRunway() {
        return aircraftBlockingRunway;
    }

    /**
     * Nastavuje volací znak lietadla, ktoré blokuje dráhu.
     * 
     * @param aircraftBlockingRunway Volací znak lietadla, ktoré blokuje dráhu.
     */
    public void setAircraftBlockingRunway(String aircraftBlockingRunway) {
        this.aircraftBlockingRunway = aircraftBlockingRunway;
    }

}
