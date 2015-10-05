package net.robobalasko.letiskoserv.navigacia;

import java.io.Serializable;

/**
 * Trieda abstrahujúca skutočné GPS
 * koordináty pre objekty na letisku a letisko samotné.
 * 
 * @author rbalasko
 */
public final class GPSCoordinates implements Serializable {
    
    /**
     * Desatinná hodnota zemepisnej výšky bodu.
     */
    private double latitude;
    
    /**
     * Desatinná hodnota zemepisnej šírky bodu.
     */
    private double longitude;
    
    /**
     * Základný konštruktor vytvára nový objekt GPS koordinátov,
     * ktorým po kontrole dát nastaví požadované hodnoty šírky a výšky.
     * 
     * @param latitude Desatinná hodnota zemepisnej výšky v rozsahu [-90,90].
     * @param longitude Desatinná hodnota zemepisnej šírky v rozsahu [-180,180].
     * 
     * @throws InvalidGPSCoordinatesException Vyhodená ak niektorý z údajov
     *         presiadne maximálnu alebo minimálnu možnú hranicu v intervale.
     */
    public GPSCoordinates(double latitude, double longitude)
	    throws InvalidGPSCoordinatesException {
	setLatitude(latitude);
	setLongitude(longitude);
    }
    
    public GPSCoordinates() {
    }
    
    /**
     * Kontroluje, či hodnota zadaná v parametri spadá do intervalu
     * hodnôt pre zemepisnú výšku.
     * 
     * @param latitude Desatinná hodnota zemepisnej výšky.
     * 
     * @return True, ak je hodnota v intervale, False inak.
     */
    private boolean checkLatitude(double latitude) {
	return latitude < 90.0 && latitude > -90.0;
    }
    
    /**
     * Kontroluje, či hodnota zadaná v parametri spadá do intervalu
     * hodnôť pre zemepisnú šírku.
     * 
     * @param longitude Desatinná hodnota zemepisnej šírky.
     * 
     * @return True, ak je hodnota v intervale, False inak.
     */
    private boolean checkLongitude(double longitude) {
	return longitude < 180.0 && longitude > -180.0;
    }

    /**
     * Vráti textovú reprezentáciu GPS koordinátu,
     * napr. N48.10 E24.32
     * 
     * @return Reťazec s textovou reprezencáciou koordinátu.
     */
    @Override
    public String toString() {
	return latitude + ((latitude > 0) ? "N" : "S") + " "
		+ longitude + ((longitude > 0) ? "E" : "W");
    }

    /**
     * Vráti hodnotu zemepisnej výšky koordinátu.
     * 
     * @return Desatinná hodnota zemepisnej výšky.
     */
    public double getLatitude() {
	return latitude;
    }

    /**
     * Nastavuje hodnotu zemepisnej výšky koordinátu.
     * 
     * @param latitude Desatinná hodnota zemepisnej výšky.
     * 
     * @throws InvalidGPSCoordinatesException Vyhodená ak hodnota predaná
     *         v parametri sa nenachádza v intervale definovanom pre správne
     *         hodnoty zemepisnej výšky.
     */
    public void setLatitude(double latitude) throws InvalidGPSCoordinatesException {
	if (!checkLatitude(latitude)) {
	    throw new InvalidGPSCoordinatesException();
        }
	this.latitude = latitude;
    }

    /**
     * Vráti hodnotu zemepisnej šírky koordinátu.
     * 
     * @return Desatinná hodnota zemepisnej výšky.
     */
    public double getLongitude() {
	return longitude;
    }

    /**
     * Nastavuje hodnotu zemepisnej šírky koordinátu.
     * 
     * @param longitude Desatinná hodnota zemepisnej výšky.
     * 
     * @throws InvalidGPSCoordinatesException Vyhodená ak hodnota predaná
     *         v parametri sa nenachádza v intervale definovanom pre správne
     *         hodnoty zemepisnej šírky.
     */
    public void setLongitude(double longitude) throws InvalidGPSCoordinatesException {
	if (!checkLongitude(longitude)) {
	    throw new InvalidGPSCoordinatesException();
	}
	this.longitude = longitude;
    }
    
}
