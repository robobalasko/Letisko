package net.robobalasko.letisko.siet;

import java.util.LinkedList;
import java.util.List;
import net.robobalasko.letiskoserv.data.AirportServiceProtocol;
import net.robobalasko.letiskoserv.lietadla.Aircraft;

/**
 * Trieda, ktorá pre klientskú časť zabezpečuje periodické prepisovanie
 * zoznamu aktuálnych lietadiel, ktoré sa práve nachádzajú na letisku
 * do zoznamov odletov / príletov a obrazovky radaru.
 *
 * @author rbalasko
 */
public class AircraftHandler implements Runnable {

    /**
     * Zoznam lietadiel, ktoré boli modifikované riadiacim
     * na danom letisku počas svojho letu.
     */
    private final List<Aircraft> modifiedAircraft;

    /**
     * Letiskový klient, ktorý zabezpečuje pre klientskú časť
     * celú komunikáciu s riadiacim serverom.
     */
    private final AirportClient airportClient;

    /**
     * Zoznam lietadiel, ktoré sa aktuálne nachádzajú
     * na letisku a sú riadené riadiacim serverom.
     *
     * Tento zoznam si periodicky každú sekundu pýtajú zoznamy odletov / príletov,
     * ktoré podľa nich do seba radia jednotlivé lietadla.
     */
    private List<Aircraft> handledAircraft;

    /**
     * Objekt vlákna, na ktorom {@code AircraftHandler} beží.
     *
     * Vlákno je naštartované z triedy {@code RadarControllerScreenFrame},
     * ktorá ho zapína hneď ako sa zobrazí vykreslené okno radaru.
     */
    private final Thread thread;

    /**
     * Údaj, či vlákno práve beží alebo nie.
     */
    private boolean running;

    /**
     * Základný konštruktor nastavuje atribúty pre {@code AirportClient},
     * ktorý je predaný z hlavného okna radaru a spájaný zoznam, do ktorého sa z radaru
     * pridávajú lietadlá modifikované riadiacim.
     *
     * @param airportClient Letiskový klient, ktorý zabezpečuje komunikáciu so serverom.
     * @param modifiedAircraft Spájaný zoznam lietadiel modifikovaných riadiacim.
     */
    public AircraftHandler(AirportClient airportClient, List<Aircraft> modifiedAircraft) {
        this.airportClient = airportClient;
        this.modifiedAircraft = modifiedAircraft;
        handledAircraft = new LinkedList<Aircraft>();
        thread = new Thread(this);
    }

    /**
     * Naštartuje vlákno, na ktorom beží {@code AircraftHandler}.
     */
    public synchronized void start() {
        running = true;
        thread.start();
    }

    /**
     * Zastaví vlákno, na ktorom beží {@code AircraftHandler}.
     */
    public synchronized void stop() {
        running = false;
        thread.interrupt();
    }

    /**
     * Hlavná metóda vlákna, ktorá si periodicky každú sekundu
     * vyžiada od servera zoznam lietadiel, ktoré sa práve na letisku
     * nachádzajú a odošle mu späť zoznam modifikovaných lietadiel.
     *
     * Zoznam modifikovaných lietadiel sa odosiela aj keď sa v ňom aktuálne
     * nenachádza žiadne lietadlo. Server však reaguje na zoznam len keď treba
     * vykonať s nejakým modifikovaným lietadlom akciu.
     */
    @Override
    public void run() {
        long timerLast = System.currentTimeMillis();
        do {
            long timerNow = System.currentTimeMillis();
            double difference = timerNow - timerLast;
            while (difference >= 500) {
                handledAircraft = airportClient.requestAircraftsData();
                airportClient.sendModifiedAircraft(modifiedAircraft);
                timerLast = timerNow;
                difference = 0;
            }
        } while (running && airportClient.getProtocol().getState() != AirportServiceProtocol.END_COM);
    }

    /**
     * Získa všetky lietadlá, ktoré sa práve nachádzajú
     * na letisku a sú automaticky riadené serverom.
     *
     * @return Spájaný zoznam objektov {@code Aircraft}
     */
    public List<Aircraft> getHandledAircraft() {
        return handledAircraft;
    }

}
