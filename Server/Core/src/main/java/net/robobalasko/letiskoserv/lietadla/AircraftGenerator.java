package net.robobalasko.letiskoserv.lietadla;

import java.util.List;
import java.util.Random;
import net.robobalasko.letiskoserv.navigacia.Airport;
import net.robobalasko.letiskoserv.navigacia.AirportRouteTypesEnum;
import net.robobalasko.letiskoserv.navigacia.Route;

/**
 * Trieda, ktorá pre každé radarové stanovište náhodne generuje lietadlá.
 *
 * @author rbalasko
 */
public class AircraftGenerator {

    /**
     * ICAO kód letiska, pre ktorý tento generátor bude generovať letiská.
     */
    private final String airportICAO;

    /**
     * Zoznam letísk, po ktorých môžu lietadla medzi radarmi lietať.
     */
    private final List<Airport> connectedAirports;

    /**
     * Letisko, s ktorým generátor pracuje po svojom zapnutí.
     */
    private final Airport loadedAirport;

    /**
     * Pole enumov obsahujúce všetky možné typy lietadiel,
     * ktoré je možné pre letiská generovať.
     */
    private final AircraftTypeEnum[] aircraftTypes;

    /**
     * Pole enumov obsahujúce všetky možné typy aeroliniek,
     * ku ktorým môže vygenerovaný stroj patriť.
     */
    private final AirlineIcaoCodeEnum[] airlineIcaoCodes;

    /**
     * Genetárot náhodných čísel, ktorý sa využíva
     * pre náhodné generovanie skoro vo všetkých metódach triedy.
     */
    private final Random rand;

    /**
     * Základný konštruktor triedy nastavuje polia,
     * z ktorých sa náhodne vyberajú dáta pre generovanie lietadla.
     *
     * @param airportICAO ICAO kód letiska, pre ktoré tento generátor generuje letiská
     * @param loadedAirport Letisko, s ktorým generátor pracuje po zapnutí
     * @param connectedAirports Letiská, ktoré su dostupné pre lety
     */
    public AircraftGenerator(String airportICAO, Airport loadedAirport,
            List<Airport> connectedAirports) {
        this.airportICAO = airportICAO;
        this.loadedAirport = loadedAirport;
        this.connectedAirports = connectedAirports;
        this.aircraftTypes = AircraftTypeEnum.values();
        this.airlineIcaoCodes = AirlineIcaoCodeEnum.values();
        this.rand = new Random();
    }

    /**
     * Generuje náhodné lietadlo, ktoré je potom sieťou
     * odoslané klientovi pre zobrazenie na radare.
     *
     * Typ lietadla generátor vyberá náhodne z ponuky typov lietadiel,
     * ktoré sú definované v enume {@code AircraftTypeEnum}
     *
     * @param commercial Definuje, či sa má generovať komerčný let.
     *
     * @return Vytvorené lietadlo ako {@code Aircraft} objekt.
     */
    public Aircraft generateRandomAircraft(boolean commercial) {
        Aircraft aircraft = new Aircraft();
        aircraft.setAircraftType(aircraftTypes[rand.nextInt(aircraftTypes.length)]);
        aircraft.setCallSign(generateRandomCallsign(commercial));
        aircraft.setDepAirport(airportICAO);
        aircraft.setArrAirport(generateRandomArrAirport());
        aircraft.setFinalFlightLevel(generateRandomRequestedFlightLevel(commercial));
        aircraft.setActualFlightLevel(0);
        aircraft.setFinalAirSpeed(generateRandomRequestedSpeed(commercial));
        aircraft.setActualAirSpeed(0);
        aircraft.setSidRoute(generateRandomRoute(loadedAirport, AirportRouteTypesEnum.SID));
        Airport destAirport = getDestinationAirport(aircraft);
        aircraft.setStarRoute(generateRandomRoute(destAirport, AirportRouteTypesEnum.STAR));
        aircraft.setActualRoute(aircraft.getSidRoute());
        return aircraft;
    }

    /**
     * Vygeneruje pre generované lietadlo náhodný volací znak.
     *
     * @param commercial Určuje či sa má generovať typ znaku pre komerčné lietadlo.
     *
     * @return Vygynerovaný volací znak ako reťazec.
     */
    private String generateRandomCallsign(boolean commercial) {
        int randNum = rand.nextInt(airlineIcaoCodes.length);
        String sign = (commercial)
                ? airlineIcaoCodes[randNum].toString()
                : "OM-";
        for (int i = 0; i < 3; i++) {
            if (commercial) {
                sign += rand.nextInt(9);
            } else {
                sign += (char) (rand.nextInt(('Z' - 'A') + 1) + 'A');
            }
        }
        return sign;
    }

    /**
     * Vygeneruje pre vygenerované lietadlo náhodnú požadovanú výšku letu lietadla.
     *
     * Ak je lietadlo komerčný let, generuje sa výška do hornej hranice RVSM priestoru,
     * inak sa generuje len do 8000 AMSL, čo je pod TA v SVK.
     *
     * @param commercial Definuje, či je let komerčný alebo nie.
     *
     * @return Vygenerovanú letovú hladinu ako celé číslo.
     */
    private int generateRandomRequestedFlightLevel(boolean commercial) {
        int reqFlightLevel = 0;
        int randCycle = commercial ? rand.nextInt(5) + 10 + 1 : rand.nextInt(5) + 2 + 1;
        for (int i = 0; i < randCycle; i++) {
            reqFlightLevel += 10;
        }
        return reqFlightLevel;
    }

    /**
     * Generuje pre vygenerované lietadlo náhodnú požadovanú rýchlosť letu lietadla.
     *
     * Ak je lietadlo komerčný let, generuje sa maximálna možná žiadaná rýchlosť do 500 KTAS,
     * inak do 200 KTAS. Tento spôsob sa tu využíva výlučne pre zjednodušenie simulácie a metóda
     * neberie v úvahu aktuálnu výšku lietadla a tlak vzduchu v danej výške, čo by mala.
     *
     * @param commercial Definuje, či je let komerčný alebo nie.
     *
     * @return Vygenerovaná požadovaná rýchlosť ako celé číslo.
     */
    private int generateRandomRequestedSpeed(boolean commercial) {
        int reqSpeed = 0;
        int randCycle = commercial ? rand.nextInt(15) + 15 + 1 : rand.nextInt(5) + 15 + 1;
        for (int i = 0; i < randCycle; i++) {
            reqSpeed += 10;
        }
        return reqSpeed;
    }

    /**
     * So zoznamu všetkých letísk vyberie náhodné letisko príletu.
     * 
     * @return Reťazec s ICAO kódom letiska príletu.
     */
    private String generateRandomArrAirport() {
        int random = rand.nextInt(connectedAirports.size());
        String generatedICAO = null;
        while ((generatedICAO = connectedAirports.get(random).getIcaoCode()).equals(airportICAO)) {
            random = rand.nextInt(connectedAirports.size());
        }
        return generatedICAO;
    }

    /**
     * So zoznamu všetkých trás pre letisko vygeneruje náhodnú trasu
     * podľa špecifikovaného typu trasy.
     * 
     * @param airport Letisko, z ktorého trás sa má trasa vybrať.
     * @param routeType Typ trasy aká sa má vygenerovať.
     * 
     * @return Vygenerovaná trasa ako {@code Route} objekt.
     */
    private Route generateRandomRoute(Airport airport,
            AirportRouteTypesEnum routeType) {
        Route generatedRoute = null;
        List<Route> availableRoutes = airport.getRoutes();
        int random = rand.nextInt(availableRoutes.size());
        while ((generatedRoute = availableRoutes.get(random))
                .getRouteType() != routeType) {
            random = rand.nextInt(availableRoutes.size());
        }
        return generatedRoute;
    }

    /**
     * Získa objekt letiska destinácie podľa ICAO kódu,
     * ktorý má lietadlo zapísaný vo svojom pláne.
     *
     * @param aircraft Objekt lietadla, na ktorom sa destinácia vyhľadáva.
     *
     * @return Letisko ako {@code Airport} objekt.
     */
    private Airport getDestinationAirport(Aircraft aircraft) {
        Airport airport = null;
        for (Airport arpt : connectedAirports) {
            String icao = arpt.getIcaoCode();
            if (aircraft.getArrAirport().equals(icao)) {
                airport = arpt;
            }
        }
        return airport;
    }

}
