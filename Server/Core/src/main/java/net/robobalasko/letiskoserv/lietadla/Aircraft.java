package net.robobalasko.letiskoserv.lietadla;

import java.awt.Point;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import net.robobalasko.letiskoserv.navigacia.AirportRouteTypesEnum;
import net.robobalasko.letiskoserv.navigacia.Route;

/**
 * Trieda opisujuca vseobecne a sukromne lietadlo.
 *
 * @author rbalasko
 */
public class Aircraft implements Serializable {

    /**
     * Typ daneho lietadla.
     *
     * @see AircraftTypeEnum
     */
    private AircraftTypeEnum aircraftType;

    /**
     * Volací znak lietadla, ktorý sa zobrazuje radaru ako jeho identifikácia.
     */
    private String callSign;

    /**
     * ICAO kód letiska, z ktorého lietadlo odlietalo.
     */
    private String depAirport;

    /**
     * ICAO kód letiska, na ktoré lietadlo smeruje.
     */
    private String arrAirport;

    /**
     * Trasa, ktorou bude lietadlo z letiska, na ktorom sa vytvorí odlietať.
     */
    private Route sidRoute;

    /**
     * Trasa, ktorou bude lietadlo na letisku kde smeruje prilietať.
     */
    private Route starRoute;

    /**
     * Trasa, ktorou sa lietadlo akurát po letiskovej riadenej oblasti pohybuje.
     */
    private Route actualRoute;

    /**
     * Horizontalna pozicia lietadla na radare.
     */
    private double latitude;

    /**
     * Vertikalna pozicia lietadla na radare.
     */
    private double longitude;

    /**
     * Konecna letova hladina pridelena lietadlu.
     */
    private int finalFlightLevel;

    /**
     * Aktualna letova hladina, v ktorej lietadlo leti.
     */
    private int actualFlightLevel;

    /**
     * Aktuálna rýchlosť lietadla TAS v uzloch.
     */
    private int finalAirSpeed;

    /**
     * Aktualna rychlost lietadla TAS v uzloch.
     */
    private int actualAirSpeed;

    /**
     * Definuje, či je lietadlo práve zvolené riadiacim
     * na radarovom zobrazení.
     */
    private boolean isSelected;

    /**
     * Zoznam všetkých koordinátov, ktoré boli zaznamenané
     * serverom ako prejdená trasa lietadla.
     */
    private final List<Point> routeTrail;

    /**
     * Koordinát, ktorý udáva smer pre zobrazenie smerovej
     * šípky lietadla na radarovej obrazovke.
     */
    private final Point directionLine;

    /**
     * Definuje, či dané lietadlo už dostalo povolenie na odlet.
     */
    private boolean clearedForDeparture;
    
    /**
     * Definuje, či dané lietadlo je už vo fáze go around.
     */
    private boolean goingAround;

    /**
     * Základný konštruktor vytvára nový objekt lietadla,
     * ktorému nastaví zoznam pre zaznamenávanie koordinátov
     * prejdenej trasy a objekt {@code Point} pre zaznamenanie
     * pozície smerovej šípky lietadla.
     * 
     * Všetky ostatné parametre objektu lietadla potom nastavuje
     * server pri vytváraní nového objektu lietadla pre riadené letisko.
     */
    public Aircraft() {
        routeTrail = new LinkedList<Point>();
        directionLine = new Point();
        clearedForDeparture = false;
    }

    /**
     * Vráti reťazec obsahujúci všetky body, ktoré sa nachádzajú
     * na odletovej / príletovej trase lietadla.
     * 
     * @param routeType Typ trasy, ktorej body sa majú vrátiť.
     * 
     * @return Reťazec bodov nachádzajúcich sa na trase. 
     */
    public String listRoutePoints(AirportRouteTypesEnum routeType) {
        Route route = (routeType == AirportRouteTypesEnum.SID)
                ? getSidRoute() : getStarRoute();
        String listedRoute = "";
        for (int i = 0; i < route.getRoutePoints().size(); i++) {
            listedRoute += route.getRoutePoints().get(i);
            if (i != route.getRoutePoints().size() - 1) {
                listedRoute += " -> ";
            }
        }
        return listedRoute;
    }
    
    /**
     * Vráti hodnotu, či je lietadlo vo fázy letu, kedy sa
     * pripravuje na pristátie.
     * 
     * @return True hodnota, ak je už lietadlo pripravené pristávať.
     */
    public boolean isAircraftLanding() {
        return actualRoute.getRouteType() == AirportRouteTypesEnum.STAR
                && actualRoute.getRoutePoints().size() <= 1;
    }

    /**
     * Vráti textovú reprezentáciu objektu lietadla.
     * 
     * @return Reťazec zložený z volacieho znaku, typu lietadla
     *         odletového a príletového letiska.
     */
    @Override
    public String toString() {
        return getCallSign() + " | " + getAircraftType().toString()
                + " | " + getDepAirport().toUpperCase() + " -> " + getArrAirport().toUpperCase()
                + " | " + getActualRoute().getRouteType();
    }

    /**
     * Vráti type lietadla na lete.
     * 
     * @return Konštanta z {@code AircraftTypeEnum} definujúca typ lietadla.
     */
    public AircraftTypeEnum getAircraftType() {
        return aircraftType;
    }

    /**
     * Nastavuje typ lietadla na lete.
     * 
     * @param aircraftType Typ lietadla z {@code AircraftTypeEnum} 
     */
    public void setAircraftType(AircraftTypeEnum aircraftType) {
        this.aircraftType = aircraftType;
    }

    /**
     * Vráti volací znak lietadla.
     * 
     * @return ICAO kód volacieho znaku a číslo letu. 
     */
    public String getCallSign() {
        return callSign;
    }

    /**
     * Nastavuje volací znak pre lietadlo.
     * 
     * @param callSign ICAO kód volacieho znaku a číslo letu. 
     */
    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    /**
     * Vráti letisko odletu pre lietadlo.
     * 
     * @return ICAO kód letiska odletu. 
     */
    public String getDepAirport() {
        return depAirport;
    }

    /**
     * Nastavuje letisko odletu pre lietadlo.
     * 
     * @param depAirport ICAO kód letiska odletu.
     */
    public void setDepAirport(String depAirport) {
        this.depAirport = depAirport;
    }

    /**
     * Vráti letisko príletu pre lietadlo.
     * 
     * @return ICAO kód letiska príletu. 
     */
    public String getArrAirport() {
        return arrAirport;
    }

    /**
     * Nastavuje letisko príletu pre lietadlo.
     * 
     * @param arrAirport ICAO kód letiska príletu.
     */
    public void setArrAirport(String arrAirport) {
        this.arrAirport = arrAirport;
    }

    /**
     * Vráti objekt s odletovou trasou lietadla.
     * 
     * @return Objekt {@code Route} s odletovou trasout lietadla. 
     */
    public Route getSidRoute() {
        return sidRoute;
    }

    /**
     * Nastavuje odletovú trasu lietadla.
     * 
     * @param sidRoute Objekt {@code Route} s odletovou trasou lietadla.
     */
    public void setSidRoute(Route sidRoute) {
        this.sidRoute = sidRoute;
    }

    /**
     * Vráti objekt s príletovou trasou lietadla.
     * 
     * @return Objekt {@code Route} s príletovou trasou lietadla.
     */
    public Route getStarRoute() {
        return starRoute;
    }

    /**
     * Nastavuje príletovú trasu lietadla.
     * 
     * @param starRoute Objekt {@code Route} s príletovou trasou lietadla.
     */
    public void setStarRoute(Route starRoute) {
        this.starRoute = starRoute;
    }

    /**
     * Vráti objekt s bodmi aktuálnej trasy lietadla.
     * 
     * @return Objekt {@code Route} s bodmi aktuálnej trasy lietadla. 
     */
    public Route getActualRoute() {
        return actualRoute;
    }

    /**
     * Nastavuje aktuálnu trasu lietadla vo vzdušnom priestore.
     * 
     * @param actualRoute Objekt {@code Route} s bodmi aktuálnej trasy lietadla. 
     */
    public void setActualRoute(Route actualRoute) {
        this.actualRoute = actualRoute;
    }

    /**
     * Vráti vertikálnu pozíciu lietadla vo vzdušnom priestore.
     * 
     * @return Vertikálna pozícia lietadla vo vzdušnom priestore. 
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Nastavuje vertikálnu pozíciu lietadla vo vzdušnom priestore.
     * 
     * @param latitude Vertikálna pozícia lietadla vo vzdušnom priestore. 
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Vráti horizontálnu pozíciu lietadla vo vzdušnom priestore
     * 
     * @return Horizontálna pozícia lietadla vo vzdušnom priestore. 
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Nastavuje horizontálnu pozíciu lietadla vo vzdušnom priestore.
     * 
     * @param longitude Hodnota horizontálnej pozície lietadla.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Vráti maximálnu možnú výšku lietadla ako násobok letovej hladiny.
     * 
     * @return Maximálna možná výška lietadla ako násobok letovej hladiny. 
     */
    public int getFinalFlightLevel() {
        return finalFlightLevel;
    }

    /**
     * Nastavuje maximálnu možnú výšku lietadla ako násobok letovej hladiny.
     * 
     * @param finalFlightLevel Maximálna možná výška lietadla ako násobok letovej hladiny.
     */
    public void setFinalFlightLevel(int finalFlightLevel) {
        this.finalFlightLevel = finalFlightLevel;
    }

    /**
     * Vráti aktuálnu výšku lietadla ako násobok letovej hladiny.
     * 
     * @return Aktiálna výška lietadla ako násobok letovej hladiny. 
     */
    public int getActualFlightLevel() {
        return actualFlightLevel;
    }

    /**
     * Nastavuje aktuálnu výšku lietadla v násobkoch letovej hladiny.
     * 
     * @param actualFlightLevel Aktuálna výška lietadla ako násobok letovej hladiny.
     */
    public void setActualFlightLevel(int actualFlightLevel) {
        this.actualFlightLevel = actualFlightLevel;
    }

    /**
     * Vráti maximálnu možnú rýchlosť lietadla v KTAS.
     * 
     * @return Maximálna možná rýchlosť lietadla v KTAS.
     */
    public int getFinalAirSpeed() {
        return finalAirSpeed;
    }

    /**
     * Nastavuje maximálnu možnú rýchlosť lietadla v KTAS.
     * 
     * @param finalAirSpeed Hodnota aktuálnej možnej rýchlosti lietadla v KTAS. 
     */
    public void setFinalAirSpeed(int finalAirSpeed) {
        this.finalAirSpeed = finalAirSpeed;
    }

    /**
     * Vráti aktuálnu rýchlosť lietadla v KTAS:
     * 
     * @return Aktuálna rýchlosť lietadla v KTAS. 
     */
    public int getActualAirSpeed() {
        return actualAirSpeed;
    }

    /**
     * Nastavuje aktuálnu rýchlosť lietadla.
     * 
     * @param actualAirSpeed Hodnota aktuálnej rýchlosti lietadla v KTAS. 
     */
    public void setActualAirSpeed(int actualAirSpeed) {
        this.actualAirSpeed = actualAirSpeed;
    }

    /**
     * Vráti hodnotu, či je lietadlo vybraté alebo nie.
     * 
     * @return True ak je lietadlo vo vybratom stave. 
     */
    public boolean isIsSelected() {
        return isSelected;
    }

    /**
     * Nastavuje, či je lietadlo aktuálne vo vybratom stave.
     * 
     * @param isSelected Definuje, či sa má lietadlo označiť ako vybraté.
     */
    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    /**
     * Vráti zoznam všetkých bodov, ktoré boli zaznamená
     * na trase, ktorú lietadlo už preletelo.
     * 
     * @return Spájaný zoznam s koordinátmi preletenej trasy.
     */
    public List<Point> getRouteTrail() {
        return routeTrail;
    }

    /**
     * Vráti bod, na ktorý smeruje smerová šípka lietadla.
     * 
     * @return koordináty smerovej šípky lietadla.
     */
    public Point getDirectionLine() {
        return directionLine;
    }

    /**
     * Vráti hodnotu, či lietadlu už bol povolený odlet.
     * 
     * @return Definuje, či už lietadlu bol povolený štart. 
     */
    public boolean isClearedForDeparture() {
        return clearedForDeparture;
    }

    /**
     * Nastavuje povolenie štartu lietadlu z letiska.
     * 
     * @param clearedForDeparture True ak je lietadlu povolené vzlietnuť.
     */
    public void setClearedForDeparture(boolean clearedForDeparture) {
        this.clearedForDeparture = clearedForDeparture;
    }

    /**
     * Vráti hodnotu, či lietadlo ide na G/A.
     * 
     * @return Definuje, či už lietadlo letí na G/A bod.
     */
    public boolean isGoingAround() {
        return goingAround;
    }

    /**
     * Nastavuje prechod lietadla do fázy letu G/A.
     * 
     * @param goingAround True ak má lietadlo letieť G/A.
     */
    public void setGoingAround(boolean goingAround) {
        this.goingAround = goingAround;
    }

}
