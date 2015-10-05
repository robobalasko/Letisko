package net.robobalasko.letiskoserv.navigacia;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Trieda opisujúca letové body z trasy letového plánu lietadla,
 * ktorými na radare lietadlo prechádza.
 *
 * Z tejto triedy dedí trieda {@link VorWaypoint},
 * ktorá túto základnú dopĺňa o frekvenciu.
 *
 * @author rbalasko
 */
public class Waypoint implements Serializable {

    /**
     * Názov letového bodu na trase.
     */
    private String name;

    /**
     * GPS pozícia letového bodu.
     */
    private GPSCoordinates gpsCoordinates;

    /**
     * Zemepisná šírka letového bodu prepočítaná do pixelov.
     */
    private int pixelCoordX;

    /**
     * Zemepisná výška letového bodu prepočítaná do pixelov.
     */
    private int pixelCoordY;

    /**
     * Základný konštruktor triedy nastavuje objektu Waypoint jeho názov,
     * zemepisnú šírku a výšku, z ktorých sa potom vytvorí objekt {@code GPSCoordinates}.
     *
     * @param name Meno letového bodu, ktoré sa zobrazuje na radare.
     * @param latitude Zemepisná čírka letového bodu.
     * @param longitude Zemepisná dĺžka letového bodu.
     */
    public Waypoint(String name, double latitude, double longitude) {
        this.name = name;
        setGpsCoordinates(latitude, longitude);
    }

    /**
     * Vráti textovú reprezentáciu letového bodu: názov.
     * 
     * @return Reťazec s názvom letového bodu.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Vráti názov letového bodu.
     * 
     * @return Reťazec s názvom letového bodu. 
     */
    public String getName() {
        return name;
    }

    /**
     * Nastavuje letovému bodu názov.
     *
     * @param name Názov, ktorý sa má nastaviť.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Vracia GPS koordináty letového bodu.
     *
     * @return Koordináty bodu v podobe {@link GPSCoordinates} objektu.
     */
    public GPSCoordinates getGpsCoordinates() {
        return gpsCoordinates;
    }

    /**
     * Nastavuje letovému bodu GPS koordináty, taktiež kontroluje,
     * či sú zadané hodnoty šírky a výšky v požadovanom rozmedzí.
     *
     * @param latitude Zemepisná šírka letového bodu.
     * @param longitude Zemepisná dĺžka letového bodu.
     */
    public final void setGpsCoordinates(double latitude, double longitude) {
        try {
            this.gpsCoordinates = new GPSCoordinates(latitude, longitude);
        } catch (InvalidGPSCoordinatesException ex) {
            Logger.getLogger(Waypoint.class.getName()).log(Level.SEVERE,
                    "The GPS coordinates you are trying to set are not within the valid range.", ex);
        }
    }

    /**
     * Vráti celočíselnú hodnotu zemepisnej šírky bodu v pixeloch.
     * 
     * @return Celočíselná hodnota zemepisnej šírky v pixeloch.
     */
    public int getPixelCoordX() {
        return pixelCoordX;
    }

    /**
     * Nastaví zemepisnú šírku bodu prepočítanú do pixelov.
     * 
     * @param pixelCoordX Zemepisná šírka bodu prepočítaná do pixelov.
     */
    public void setPixelCoordX(int pixelCoordX) {
        this.pixelCoordX = pixelCoordX;
    }

    /**
     * Vráti celočíselnú hodnotu zemepisnej výšky bodu v pixeloch.
     * 
     * @return Celočíselná hodnota zemepisnej šírky v pixeloch.
     */
    public int getPixelCoordY() {
        return pixelCoordY;
    }

    /**
     * Nastaví zemepisnú šírku bodu prepočítanú do pixelov.
     * 
     * @param pixelCoordY Celočíselná hodnota zemepisnej výšky v pixeloch.
     */
    public void setPixelCoordY(int pixelCoordY) {
        this.pixelCoordY = pixelCoordY;
    }

}
