package net.robobalasko.letiskoserv.navigacia;

import java.io.Serializable;
import java.util.List;

/**
 * Trieda definujúca cestovnú trasu, po ktorej lietadlo na radare letí
 * a taktiež ju je možné z radaru zobraziť.
 *
 * @author rbalasko
 */
public class Route implements Serializable {

    /**
     * Číslo dráhy, ku ktorej trasa patrí.
     */
    private final int runwayNumber;

    /**
     * Identifikačný názov letovej trasy.
     */
    private final String routeName;

    /**
     * Typ trasy [SID - odletová, STAR - príletová].
     */
    private final AirportRouteTypesEnum routeType;

    /**
     * Zoznam názvov všetkých bodov, kotré sa na trase nachádzajú.
     */
    private List<String> routePoints;

    /**
     * Základný konštruktor nastavuje trase číslo dráhy, ku ktorej patrí,
     * identifikačný názov trasy, typ trasy a zoznam bodov ležiacich na trase.
     *
     * @param runwayNumber Číslo dráhy, ku ktorej trasa patrí.
     * @param routeName Identifikačný názov trasy.
     * @param routeType Typ trasy (SID / STAR).
     * @param routePoints Letové body, ktoré sa na trase nachádzajú.
     */
    public Route(int runwayNumber, String routeName,
            AirportRouteTypesEnum routeType, List<String> routePoints) {
        this.runwayNumber = runwayNumber;
        this.routeName = routeName;
        this.routeType = routeType;
        this.routePoints = routePoints;
    }

    /**
     * Vráti identifikačný názov trasy.
     * 
     * @return Názov trasy.
     */
    public String getRouteName() {
        return routeName;
    }

    /**
     * Vráti typ trasy.
     * 
     * @return Typ trasy podľa {@code AirportRouteTypesEnum}.
     */
    public AirportRouteTypesEnum getRouteType() {
        return routeType;
    }

    /**
     * Nastavuje zoznam bodov, ktoré ležia na trase
     *
     * @param routePoints Zoznam bodov, ktoré sa majú pre trasu nastaviť.
     */
    public void setRoutePoints(List<String> routePoints) {
        this.routePoints = routePoints;
    }

    /**
     * Vráti zoznam bodov, ktoré sa na trase nachádzajú.
     * @return Zoznam bodov na trase.
     */
    public List getRoutePoints() {
        return routePoints;
    }

}
