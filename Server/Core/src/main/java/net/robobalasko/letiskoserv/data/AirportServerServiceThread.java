package net.robobalasko.letiskoserv.data;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.robobalasko.letiskoserv.lietadla.Aircraft;
import net.robobalasko.letiskoserv.lietadla.AircraftGenerator;
import net.robobalasko.letiskoserv.navigacia.Airport;
import net.robobalasko.letiskoserv.navigacia.AirportRouteTypesEnum;
import net.robobalasko.letiskoserv.navigacia.InvalidRouteDataException;
import net.robobalasko.letiskoserv.navigacia.InvalidRunwayDataException;
import net.robobalasko.letiskoserv.navigacia.InvalidWaypointDataException;
import net.robobalasko.letiskoserv.navigacia.NextWaypointDirectionEnum;
import net.robobalasko.letiskoserv.navigacia.Route;
import net.robobalasko.letiskoserv.navigacia.Waypoint;

/**
 * Trieda zabezpečujúca komunikáciu medzi letiskovým klientom a hlavným serverom, ktorý obsluhuje všeky pripojené letiská.
 *
 * @author rbalasko
 */
public class AirportServerServiceThread extends Thread {

    /**
     * Poradové číslo vytvoreného klienta serverom.
     */
    private final int threadNumber;

    /**
     * Súbor s jazykovým prekladom.
     */
    private final ResourceBundle language;

    /**
     * Súbor s nastaveniami aplikácie.
     */
    private final ResourceBundle settings;

    /**
     * Socket klienta, s ktorým dané vlákno komunikuje.
     */
    private final Socket socket;

    /**
     * Protokol komunikácie medzi klientom a serverom.
     */
    private final AirportServiceProtocol protocol;

    /**
     * Dáta prichádzajúce od servera.
     */
    private final ObjectInputStream incomingData;

    /**
     * Dáta odchádzajúce zo servera klientovi.
     */
    private final ObjectOutputStream outgoingData;

    /**
     * Zoznam aktuálne dostupných letísk.
     */
    private final List<String> availableAirports;

    /**
     * Načítavanie letísk z xml súborov.
     */
    private final AirportLoader airportLoader;

    /**
     * ICAO kód letiska, ktoré má klient zapnuté.
     */
    String requestedArptIcao;

    /**
     * Dáta letiska, ktoré má klient zapnuté.
     */
    Airport loadedAirport;

    /**
     * Generátor náhodných lietadiel pre radarové stanovište.
     */
    AircraftGenerator aircraftGenerator;

    /**
     * Zoznam všetkých pripojených letísk.
     */
    private final List<Airport> connectedAirports;

    /**
     * Globálny zoznam vygenerovaných lietadiel na všetkých letiskách.
     */
    private final List<List<Aircraft>> globalGeneratedAircraft;

    /**
     * Zoznam všetkých vygenerovaných lietadiel na tomto letisku.
     */
    private final List<Aircraft> generatedAircraft;

    /**
     * Konštanta určujúca maximálny počet odlietajúcich lietadiel z letiska.
     */
    private final int MAX_DEP_AIRCRAFT = 5;

    /**
     * Šírka radarovej obrazovky, na ktorej sa zobrazujú lietadlá.
     */
    private final int DISPLAY_WIDTH;

    /**
     * Výška radarovej obrazovky, na ktorej sa zobrazujú lietadlá.
     */
    private final int DISPLAY_HEIGHT;

    /**
     * Interval, v ktorom server odosiela lietadlá klientským radarom.
     */
    private final int REFRESH_INTERVAL;

    /**
     * Základný konštruktor nastavuje všetky potrebné atribúty pre vlákno komunikujúce s klientom.
     *
     * @param socket Objekt socketu, ku ktorému je klient pripojený
     * @param availableAirports Zoznam dostupných letísk, ktoré server načítal
     * @param language Súbor s jazykovým prekladom
     * @param settings Súbor s nastaveniami aplikácie
     * @param threadNumber Poradové číslo vlákna na server (identifikačný údaj)
     * @param airportLoader Objekt, ktorý sa stará o načítavanie dát letísk z xml súborov
     * @param globalGeneratedAircraft Zoznam všetkých lietadiel na všetkých letiskách
     * @param connectedAirports Zoznam všetkých pripojených letísk k serveru
     *
     * @throws IOException Podmienka je vyhodená, ak sa nepodarí vytvoriť I/O streamy, cez ktoré {@code socket} komunikuje.
     */
    public AirportServerServiceThread(
            Socket socket,
            List<String> availableAirports,
            ResourceBundle language,
            ResourceBundle settings,
            int threadNumber,
            AirportLoader airportLoader,
            List<List<Aircraft>> globalGeneratedAircraft,
            List<Airport> connectedAirports) throws IOException {
        this.threadNumber = threadNumber;
        this.language = language;
        this.settings = settings;
        this.socket = socket;
        this.protocol = new AirportServiceProtocol();
        this.outgoingData = new ObjectOutputStream(socket.getOutputStream());
        this.incomingData = new ObjectInputStream(socket.getInputStream());
        this.availableAirports = availableAirports;
        this.airportLoader = airportLoader;
        this.globalGeneratedAircraft = globalGeneratedAircraft;
        this.generatedAircraft = globalGeneratedAircraft.get(threadNumber);
        this.connectedAirports = connectedAirports;
        this.DISPLAY_WIDTH = Integer.parseInt(settings.getString("settings.screen_width"));
        this.DISPLAY_HEIGHT = Integer.parseInt(settings.getString("settings.screen_height"));
        this.REFRESH_INTERVAL = Integer.parseInt(settings.getString("settings.refresh_interval"));
    }

    /**
     * Metóda, ktorá po spustení vlákna obsluhuje celú komunikáciu servera s klientom.
     *
     * Hneď po pripojení sa klienta na server sa spustí táto metóda, ktorá nastavením statusu na {@code WAITING} v konštruktore objektu vlákna čaká kým klient nepošle žiadosť o zoznam dostupných letísk na pripojenie.
     *
     * Po odoslaní zoznamu letísk sa vlákno nastaví do stavu {@code SEND_AIRPRT_LIST}, ktorý značí, že znova je nutné čakať kým sa klient neozve so žiadosťou o dáta s príslušným letiskom, ktoré si na svojej strane vybral. Server klientovi letisko načíta pomocou triedy {@code AirportLoader}, dáta uloží do vytvoreného objektu typu {@code Airport} a odošle ho klientovi. Následne zmení svoj stav na {@code SENT_AIRPRT_DATA}
     */
    @Override
    public void run() {
        try {
            // Premenná so signálnymi protokolu o aké služby práva klient žiada
            Integer requestFlag = null;
            // Čakanie na žiadosť o odoslanie zoznamu s dostupnými letiskami
            sendAvailableAirportsRequest(requestFlag);
            // Čakanie na žiadosť o odoslanie dát o letisku
            sendAirportDataRequest(requestFlag);
            // Komunikácia s radarom klienta
            clientRadarControllingRequest(requestFlag);
            // Ukončenie spojenia s klientom
            closeConnection();
        } catch (EmptyRouteException ex) {
            Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WaypointNotLoadedException ex) {
            Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Odosiela klientovi zoznam letísk, ku ktorým sa je momentálne možné pripojiť.
     *
     * @param requestFlag Kód požiadavky od klienta.
     */
    public void sendAvailableAirportsRequest(Integer requestFlag) {
        while (protocol.getState() == AirportServiceProtocol.WAITING) {
            try {
                requestFlag = (Integer) incomingData.readObject();

                // Odoslanie zoznamu letísk, ku ktorým sa je možné pripojiť
                if (requestFlag == AirportServiceProtocol.SEND_AIRPRT_LIST) {
                    System.out.println("Prišla žiadosť! Odosielam zoznam voľných letísk...");
                    outgoingData.writeObject(availableAirports);

                    System.out.println("Mením stav protokolu...");
                    protocol.setState(AirportServiceProtocol.SENT_AIRPRT_LIST);
                }
            } catch (IOException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Prijme od klienta aktuálnu veľkosť jeho zobrazenia na radarovej obrazovke, aby bolo možné pre vygenerovanie objektus letiskom prepočítať skutočné dáta na pixelové dáta pre radarovú obrazovku.
     *
     * @return Pole s hodnotami šírky a výšky.
     */
    public int[] receiveRadarScreenSizeInformation() {
        int[] radarScreenSize = new int[2];
        while (protocol.getState() == AirportServiceProtocol.SENT_AIRPRT_LIST) {
            try {
                Integer requestFlag = (Integer) incomingData.readObject();
                if (requestFlag == AirportServiceProtocol.SENT_GUISCR_DATA) {
                    radarScreenSize[0] = (Integer) incomingData.readObject();
                    radarScreenSize[1] = (Integer) incomingData.readObject();
                }
                protocol.setState(AirportServiceProtocol.RCVD_GUISCR_DATA);
            } catch (IOException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return radarScreenSize;
    }

    /**
     * Odošle klientovi objekt s načítaným letiskom, o ktoré požiadal.
     *
     * @param requestFlag Kód požiadavky od klienta.
     */
    public void sendAirportDataRequest(Integer requestFlag) {
        int[] radarScreenSize = receiveRadarScreenSizeInformation();
        while (protocol.getState() == AirportServiceProtocol.RCVD_GUISCR_DATA) {
            try {
                // Odoslanie dát o letisku
                requestFlag = (Integer) incomingData.readObject();
                if (requestFlag == AirportServiceProtocol.SEND_AIRPRT_DATA) {
                    System.out.println("Prišla žiadosť o letisko! Čakám na jeho kód...");
                    requestedArptIcao = (String) incomingData.readObject();

                    System.out.println("Klient žiada o: " + requestedArptIcao);
                    loadedAirport = airportLoader.loadAirport(requestedArptIcao, radarScreenSize);
                    connectedAirports.add(loadedAirport);

                    System.out.println("Odosielam objekt žiadaného letiska!");
                    outgoingData.writeObject(loadedAirport);

                    makeAirportUnavailable();

                    System.out.println("Mením stav protokolu...");
                    protocol.setState(AirportServiceProtocol.SENT_AIRPRT_DATA);
                }
            } catch (IOException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidRunwayDataException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidRouteDataException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidWaypointDataException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Odstráni aktuálne obsadzované letísko klientom so zoznamu dostupných letísk pre pripojenie.
     */
    private void makeAirportUnavailable() {
        for (int i = 0; i < availableAirports.size(); i++) {
            String airport = availableAirports.get(i);
            if (airport.equals(requestedArptIcao)) {
                availableAirports.remove(airport);
                System.out.println("Označil som letisko " + requestedArptIcao + " ako obsadené!");
            }
        }
    }

    /**
     * Metóda, ktorá zabezpečeuje automatickú letovú prevádzku, generuje a riadi let lietadiel podľa ich letových plánov, sleduje výšky, rýchlosti.
     *
     * @param requestFlag Kód požiadavky od klienta.
     *
     * @throws EmptyRouteException Ak sa v trase lietadla už nenájde žiaden ďalší bod a lietadlo ešte nepristáva.
     * @throws WaypointNotLoadedException Ak sa v trase lietadla nepodarí nájsť bod, ktorý definuje letisková mapa ako nasledujúci bod na ním letenej trase.
     * @throws IOException Ak sa nepodarí odoslanie / prijatie dát klientnovi.
     * @throws ClassNotFoundException Ak sa nepodarí nájsť triedu, na ktorú sa objekt prijatý od klienta má pretypovať.
     */
    private synchronized void clientRadarControllingRequest(Integer requestFlag)
            throws EmptyRouteException,
            WaypointNotLoadedException,
            IOException,
            ClassNotFoundException {
        aircraftGenerator = new AircraftGenerator(requestedArptIcao, loadedAirport, connectedAirports);
        long lastTimer = System.currentTimeMillis();
        long lastPassRouteTimer = System.currentTimeMillis();
        while (protocol.getState() == AirportServiceProtocol.SENT_AIRPRT_DATA) {
            try {
                long currTimer = System.currentTimeMillis();
                double timerDifference = System.currentTimeMillis() - lastPassRouteTimer;
                if (currTimer - lastTimer >= REFRESH_INTERVAL) {
                    requestFlag = (Integer) incomingData.readObject();
                    if (requestFlag == AirportServiceProtocol.END_COM) {
                        System.out.println("Server posial žiadosť o ukončenie komunikácie...");
                        availableAirports.add(requestedArptIcao);
                        System.out.println("Letisko " + requestedArptIcao + " bolo uvoľnené...");
                        protocol.setState(AirportServiceProtocol.END_COM);
                        break;
                    }
                    if (requestFlag == AirportServiceProtocol.SEND_AIRCFT_DATA) {
                        sendAircraftToClient(timerDifference);
                    }

                    requestFlag = (Integer) incomingData.readObject();
                    if (requestFlag == AirportServiceProtocol.END_COM) {
                        System.out.println("Server posial žiadosť o ukončenie komunikácie...");
                        availableAirports.add(requestedArptIcao);
                        System.out.println("Letisko " + requestedArptIcao + " bolo uvoľnené...");
                        protocol.setState(AirportServiceProtocol.END_COM);
                        break;
                    }
                    if (requestFlag == AirportServiceProtocol.RCVD_MODACFT_DATA) {
                        List<Aircraft> modifiedAircraft = (List<Aircraft>) incomingData.readObject();
                        if (!modifiedAircraft.isEmpty()) {
                            Aircraft ac = modifiedAircraft.get(0);
                            modifyControlledAircraft(ac);
                        }
                    }
                    lastTimer = currTimer;
                    if (timerDifference >= REFRESH_INTERVAL * 6) {
                        lastPassRouteTimer = System.currentTimeMillis();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Odošle celý zoznam lietadiel, ktoré sa na danom letisku nachádzajú klientovi.
     *
     * @throws EmptyRouteException Ak sa v trase lietadla už nenájde žiaden ďalší bod a lietadlo ešte nepristáva.
     * @throws WaypointNotLoadedException Ak sa v trase lietadla nepodarí nájsť bod, ktorý definuje letisková mapa ako nasledujúci bod na ním letenej trase.
     * @throws IOException Ak sa nepodarí odoslanie / prijatie dát klientnovi.
     */
    private void sendAircraftToClient(double timerDifference)
            throws IOException,
            EmptyRouteException,
            WaypointNotLoadedException {
        Random rand = new Random();
        int generatedAcftSize = generatedAircraft.size();
        if (generatedAcftSize < MAX_DEP_AIRCRAFT
                && checkRunwayClear()
                && (System.currentTimeMillis() % (rand.nextInt((generatedAcftSize + 1) * 30) + 1)) == 0) {
            System.out.println("Vygenerovalo sa nové lietadlo...");
            Aircraft acft = aircraftGenerator.generateRandomAircraft(rand.nextInt(2) > 0);
            acft.setLatitude(loadedAirport.getWaypointByName(requestedArptIcao.toUpperCase()).getPixelCoordX());
            acft.setLongitude(loadedAirport.getWaypointByName(requestedArptIcao.toUpperCase()).getPixelCoordY());
            generatedAircraft.add(acft);
            loadedAirport.setRunwayBlocked(true);
        }

        // Odoslanie aktuálneho zoznamu lietadiel na letisku
        outgoingData.writeObject(generatedAircraft);
        outgoingData.reset();

        synchronized (generatedAircraft) {
            // Modifikácia dát lietadiel na letisku
            for (int i = 0; i < generatedAircraft.size(); i++) {
                Aircraft acft = generatedAircraft.get(i);
                if (checkRunwayClear()) {
                    loadedAirport.setRunwayBlocked(false);
                    loadedAirport.setAircraftBlockingRunway("");
                }
                if (acft.isClearedForDeparture()) {
                    moveAircraft(acft, timerDifference);
                    accelerateAircraft(acft);
                    climbAircraft(acft);
                    if (isClearedToLand(acft)) {
                        loadedAirport.setRunwayBlocked(true);
                        loadedAirport.setAircraftBlockingRunway(acft.getCallSign());
                    } else {
                        if (acft.isAircraftLanding() && isWithinLandingDistance(acft)) {
                            if (loadedAirport.isRunwayBlocked()
                                    && !loadedAirport.getAircraftBlockingRunway().equals(acft.getCallSign())) {
                                acft.getActualRoute().getRoutePoints().add(
                                        loadedAirport.getRandomWaypoint(requestedArptIcao.toUpperCase()).toString()
                                );
                                acft.setGoingAround(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Ukončuje komunikáciu klienta so serverom a uzatvára socket aj IO streamy.
     */
    public void closeConnection() {
        if (protocol.getState() == AirportServiceProtocol.END_COM) {
            try {
                outgoingData.writeObject(AirportServiceProtocol.END_COM);

                outgoingData.close();
                incomingData.close();
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(AirportServerServiceThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Komunikácia skončila...");
        }
    }

    /**
     * Skontroluje, či sa v blízkosti dráhy alebo rovno na nej nenachádza práve žiadne lietadlo, aby mohlo byť vytovrené nové.
     *
     * @return True, ak je lietadlo mimo dráhy, false inak.
     */
    private boolean checkRunwayClear() {
        double airportLat = loadedAirport.getPixelCoordX();
        double airportLon = loadedAirport.getPixelCoordY();
        for (Aircraft aircraft : generatedAircraft) {
            if ((aircraft.getLatitude() >= airportLat - 50 && aircraft.getLatitude() <= airportLat + 50)
                    && (aircraft.getLongitude() >= airportLon - 50 && aircraft.getLongitude() <= airportLon + 50)
                    && aircraft.getActualFlightLevel() <= 50) {
                return false;
            }
        }
        return true;
    }

    /**
     * Zistí aký typ trasy lietadlo na letisku letí.
     *
     * @param acft Lietadlo, pre ktoré sa má údaj zistiť.
     *
     * @return Vracia údaj ako konštantu z enumu {@code AirportRouteTypesEnum}
     *
     * @see AirportRouteTypesEnum
     */
    private AirportRouteTypesEnum getAircraftRouteType(Aircraft acft) {
        return acft.getActualRoute().getRouteType();
    }

    /**
     * Získa ďalší bod na trase lietadla.
     *
     * @param acft Lietadlo, ktorého ďalší bod na trase sa má získať.
     * @param acftRouteType Typ trasy, ktorú lietadlo na letisku letí.
     *
     * @return Bod, na ktorý lietadlo bude smerovať ako {@code Waypoint} objekt.
     *
     * @throws EmptyRouteException Vyhodená ak sa na trase lietadla už nenachádzajú žiadne ďalšie body.
     * @throws WaypointNotLoadedException Vyhodená ak sa ďalší bod na trase lietadla nepodarilo nájsť.
     */
    private Waypoint getNextRouteWaypoint(Aircraft acft, AirportRouteTypesEnum acftRouteType)
            throws EmptyRouteException, WaypointNotLoadedException {
        Route acftRoute;
        Airport airport;
        if (acftRouteType == AirportRouteTypesEnum.SID) {
            acftRoute = acft.getSidRoute();
            airport = loadedAirport;
        } else {
            acftRoute = acft.getStarRoute();
            airport = getDestinationAirport(acft);
        }
        if (acftRoute.getRoutePoints().isEmpty()) {
            throw new EmptyRouteException();
        }
        String nextRouteWaypoint = (String) acftRoute.getRoutePoints().get(0);
        Waypoint loadedWaypoint = airport.getWaypointByName(nextRouteWaypoint);
        if (loadedWaypoint == null) {
            throw new WaypointNotLoadedException();
        }
        return loadedWaypoint;
    }

    /**
     * Zistenie, na ktorej svetovej strane voči aktuálnej pozícií lietadla sa nachádza nasledujúci bod na jeho trase.
     *
     * @param acft Lietadlo, pre ktoré sa má údaj zistiť.
     * @param waypoint Bod, na ktorý lietadlo bude smerovať.
     *
     * @return Svetová strana, na ktorej sa ďalší bod na trase nachádza.
     *
     * @see NextWaypointDirectionEnum
     */
    private NextWaypointDirectionEnum getNextWaypointDirection(Aircraft acft,
            Waypoint waypoint) {
        if (acft.getLatitude() < waypoint.getPixelCoordX()
                && acft.getLongitude() > waypoint.getPixelCoordY()) {
            return NextWaypointDirectionEnum.NE;
        } else if (acft.getLatitude() < waypoint.getPixelCoordX()
                && acft.getLongitude() == waypoint.getPixelCoordY()) {
            return NextWaypointDirectionEnum.E;
        } else if (acft.getLatitude() < waypoint.getPixelCoordX()
                && acft.getLongitude() < waypoint.getPixelCoordY()) {
            return NextWaypointDirectionEnum.SE;
        } else if (acft.getLatitude() == waypoint.getPixelCoordX()
                && acft.getLongitude() < waypoint.getPixelCoordY()) {
            return NextWaypointDirectionEnum.S;
        } else if (acft.getLatitude() > waypoint.getPixelCoordX()
                && acft.getLongitude() < waypoint.getPixelCoordY()) {
            return NextWaypointDirectionEnum.SW;
        } else if (acft.getLatitude() > waypoint.getPixelCoordX()
                && acft.getLongitude() == waypoint.getPixelCoordY()) {
            return NextWaypointDirectionEnum.W;
        } else {
            return NextWaypointDirectionEnum.NW;
        }
    }

    /**
     * Slúži na výpočet protiľahlej strany pravouhlého trojuholníka, pomocou ktorého sa počíta pohyb lietadla na radarovej obrazovke.
     *
     * @param aircraft Lietadlo, pre ktorého pozíciu sa dĺžka počíta.
     * @param waypoint Bod, na ktorý lietadlo práve smeruje.
     * @param waypointDirection Smer bodu voči lietadlu.
     * @return
     */
    private double calculateOppositeLength(Aircraft aircraft, Waypoint waypoint,
            NextWaypointDirectionEnum waypointDirection) {
        switch (waypointDirection) {
            case NE:
            case NW:
                return aircraft.getLongitude() - waypoint.getPixelCoordY();
            case SE:
            case SW:
                return waypoint.getPixelCoordY() - aircraft.getLongitude();
            default:
                return 0;
        }
    }

    /**
     * Slúži na výpočet priľahlej strany pravouhlého trojuholníka, pomocou ktérho sa počíta pohyb lietadla na radarovej obrazovke.
     *
     * @param aircraft Lietadlo, pre ktorého pozíciu sa dĺžka počíta.
     * @param waypoint Bod, na ktorý lietadlo práve smeruje.
     * @param waypointDirection Smer bodu voči lietadlu.
     *
     * @return Dĺžka priľahlej strany, ktorá slúži na výpočet novej pozície lietadla.
     */
    private double calculateAdjacentLength(Aircraft aircraft, Waypoint waypoint,
            NextWaypointDirectionEnum waypointDirection) {
        switch (waypointDirection) {
            case NE:
            case SE:
                return waypoint.getPixelCoordX() - aircraft.getLatitude();
            case NW:
            case SW:
                return aircraft.getLatitude() - waypoint.getPixelCoordX();
            default:
                return 0;
        }
    }

    /**
     * Vypočíta dĺžku prepony z aktuálnej pozície lietadla, voči bodu na ktorý lietadlo smeruje.
     *
     * @param oppositeLength Dĺžka protiľahlej strany trojuholníka z aktuálnej pozície.
     * @param adjacentLength Dĺžka priľahlej strany trojuholníka z aktuálnej pozície.
     *
     * @return Dĺžka prepony, ktorá slúži na výpočet novej pozície lietadla.
     */
    private double calculateHypotenuseLength(double oppositeLength, double adjacentLength) {
        return Math.sqrt(Math.pow(oppositeLength, 2) + Math.pow(adjacentLength, 2));
    }

    /**
     * Vypočíta dĺžku novej protiľahlej strany trojuholníka, pomocou ktorého sa počíta pohyb lietadla smerom k ďalšiemu bodu na jeho trase.
     *
     * @param hypotenuse Dĺžka prepony z aktuálnej pozície lietadla.
     * @param oppositeLength Dĺžka aktuálnej protiľahlej strany.
     * @param speed Rýchlosť, ktorou lietadlo práve letí.
     *
     * @return Nová dĺžka protiľahlej strany trojuholníka.
     */
    private double calculateNewOppositeLength(double hypotenuse, double oppositeLength, double speed) {
        return (hypotenuse - speed) * oppositeLength / hypotenuse;
    }

    /**
     * Vypočíta dĺžku novej priľahlej strany trojuholníka, pomocou ktorého sa počíta pohyb lietadla smerom k ďalšiemu bodu na jeho trase.
     *
     * @param hypotenuse Dĺžka prepony z aktuálnej pozície lietadla
     * @param newOppositeLength Dĺžka novej protiľahlej strany
     * @param speed Rýchlosť, ktorou lietadlo práve letí
     *
     * @return Nová dĺžka priľahlej strany trojuholníka.
     */
    private double calculateNewAdjacentLength(double hypotenuse, double newOppositeLength, double speed) {
        return Math.sqrt(Math.pow(hypotenuse - speed, 2) - Math.pow(newOppositeLength, 2));
    }

    /**
     * Získa novú X pozíciu na radarovej obrazovke podľa aktuálnej pozície lietadla, bodu a smeru, na ktorom sa nasledujúci bod nachádza.
     *
     * @param waypoint Bod, ku ktorému lietadlo smeruje.
     * @param newAdjacentLength Dĺžka novej priľahlej strany trojuholníka, pomocou ktorého sa dáta počítajú.
     * @param waypointDirection Smer, na ktorom sa nachádza bod voči lietadlu.
     *
     * @return Pozícia na obrazovke, kde sa má lietadlo pohnúť v horizontálnom smere.
     */
    private double getNewXPosition(Waypoint waypoint, double newAdjacentLength,
            NextWaypointDirectionEnum waypointDirection) {
        switch (waypointDirection) {
            case NE:
            case SE:
                return waypoint.getPixelCoordX() - newAdjacentLength;
            case SW:
            case NW:
                return waypoint.getPixelCoordX() + newAdjacentLength;
            default:
                return 0;
        }
    }

    /**
     * Získa novú pozíciu Y na radarovej obrazovke podľa aktuálnej pozície lietadla, bodu a smeru, na ktorom sa nasledujúci bod nachádza.
     *
     * @param waypoint Bod, ku ktorému lietadlo smeruje.
     * @param newOppositeLength Dĺžka novej protiľahlej strany trojuholníka, pomocou ktorého sa dáta počítajú.
     * @param waypointDirection Smer, na ktorom sa nachádza bod voči lietadlu.
     *
     * @return Pozícia na obrazovke, kde sa má lietadlo pohnúť vo vertikálnom smere.
     */
    private double getNewYPosition(Waypoint waypoint, double newOppositeLength,
            NextWaypointDirectionEnum waypointDirection) {
        switch (waypointDirection) {
            case NE:
            case NW:
                return waypoint.getPixelCoordY() + newOppositeLength;
            case SE:
            case SW:
                return waypoint.getPixelCoordY() - newOppositeLength;
            default:
                return 0;
        }
    }

    /**
     * Kontrola, či už bol zadaný bod na trase lietadla preletený alebo nie.
     *
     * @param aircraft Lietadlo, ktorého sa kontrola týka
     * @param waypoint Bod, ktorého prelet sa sleduje
     * @param speed Rýchlosť letu, ktorá slúži pre výpočet okolia bodu pre prelet
     * @param waypointDirection Smer, v ktorom sa bod voči lietadlu nachádza
     *
     * @return True ak už lietadlo bod preletelo, false inak.
     */
    private boolean checkWaypointPassed(Aircraft aircraft, Waypoint waypoint,
            double speed, NextWaypointDirectionEnum waypointDirection) {
        switch (waypointDirection) {
            case NE:
                return aircraft.getLatitude() >= waypoint.getPixelCoordX() - (speed * 2)
                        && aircraft.getLongitude() <= waypoint.getPixelCoordY() + (speed * 2);
            case SE:
                return aircraft.getLatitude() >= waypoint.getPixelCoordX() - (speed * 2)
                        && aircraft.getLongitude() >= waypoint.getPixelCoordY() - (speed * 2);
            case SW:
                return aircraft.getLatitude() <= waypoint.getPixelCoordX() + (speed * 2)
                        && aircraft.getLongitude() >= waypoint.getPixelCoordY() - (speed * 2);
            case NW:
                return aircraft.getLatitude() <= waypoint.getPixelCoordX() + (speed * 2)
                        && aircraft.getLongitude() <= waypoint.getPixelCoordY() + (speed * 2);
            default:
                return false;
        }
    }

    private boolean isWithinLandingDistance(Aircraft aircraft) {
        return aircraft.getLatitude() >= loadedAirport.getPixelCoordX() - 50
                && aircraft.getLatitude() <= loadedAirport.getPixelCoordX() + 50
                && aircraft.getLongitude() >= loadedAirport.getPixelCoordY() - 50
                && aircraft.getLongitude() <= loadedAirport.getPixelCoordY() + 50;
    }

    private boolean isClearedToLand(Aircraft aircraft) {
        return checkRunwayClear()
                && aircraft.getActualAirSpeed() < 180
                && aircraft.isAircraftLanding()
                && isWithinLandingDistance(aircraft);
    }

    private boolean isNextPointTriangular(NextWaypointDirectionEnum direction) {
        return direction == NextWaypointDirectionEnum.NE
                || direction == NextWaypointDirectionEnum.SE
                || direction == NextWaypointDirectionEnum.SW
                || direction == NextWaypointDirectionEnum.NW;
    }

    private int getNewXNonTriangularPosition(NextWaypointDirectionEnum direction,
            Aircraft aircraft, int speed) {
        switch (direction) {
            case N:
                return (int) aircraft.getLatitude();
            case E:
                return (int) aircraft.getLatitude() + speed;
            case S:
                return (int) aircraft.getLatitude();
            default:
                return (int) aircraft.getLatitude() - speed;
        }
    }

    private int getNewYNonTriangularPosition(NextWaypointDirectionEnum direction,
            Aircraft aircraft, int speed) {
        switch (direction) {
            case N:
                return (int) aircraft.getLongitude() - speed;
            case E:
                return (int) aircraft.getLongitude();
            case S:
                return (int) aircraft.getLongitude() + speed;
            default:
                return (int) aircraft.getLongitude();
        }
    }

    /**
     * Pohyb lietadla po obrazovke v smere ku nasledujúcemu bodu na trase.
     *
     * @param acft Lietadlo, ktoré sa má na radare pohnúť
     *
     * @throws EmptyRouteException Vyhodená ak sa už v cestovnej trase lietadla nenachádzajú ďalšie body, ale napriek tomu bol pokus o odobratie bodu.
     * @throws WaypointNotLoadedException Vyhodená ak sa v zozname bodov, ktoré sú na trase lietadla nepodarilo nájsť požadovaný bod.
     */
    private synchronized void moveAircraft(Aircraft acft, double timerDifference)
            throws EmptyRouteException, WaypointNotLoadedException {
        AirportRouteTypesEnum routeType = getAircraftRouteType(acft);
        Waypoint nextWpt;
        if (!acft.getActualRoute().getRoutePoints().isEmpty()) {
            nextWpt = getNextRouteWaypoint(acft, routeType);
        } else {
            if (acft.getDepAirport().equals(requestedArptIcao)
                    && acft.getActualRoute().getRouteType() == AirportRouteTypesEnum.SID) {
                // Ak už v zozname nie sú ďalšie body a lietadlo je stále riadené
                // DEP radarom, prehodíme ho na letisko pristátia
                handOffAircraft(acft);
            } else {
                // Ak už v zozname nie sú ďalšie body a lietadlo je na letisku
                // pristátia, môže pristáť, predá sa TWR riadeniu...
                acft.getRouteTrail().clear();
                globalGeneratedAircraft.get(threadNumber).remove(acft);
                loadedAirport.setRunwayBlocked(true);
                loadedAirport.setAircraftBlockingRunway(acft.getCallSign());
            }
            return;
        }
        NextWaypointDirectionEnum nextWptDir = getNextWaypointDirection(acft, nextWpt);

        int speed = (acft.getActualAirSpeed() / 100);
        double newAcftX;
        double newAcftY;
        if (isNextPointTriangular(nextWptDir)) {
            double oppositeLength = calculateOppositeLength(acft, nextWpt, nextWptDir);
            double adjacentLength = calculateAdjacentLength(acft, nextWpt, nextWptDir);
            double hypotenuse = calculateHypotenuseLength(oppositeLength, adjacentLength);
            double newOppositeLength = calculateNewOppositeLength(hypotenuse, oppositeLength, speed);
            double newAdjacentLength = calculateNewAdjacentLength(hypotenuse, newOppositeLength, speed);
            newAcftX = getNewXPosition(nextWpt, newAdjacentLength, nextWptDir);
            newAcftY = getNewYPosition(nextWpt, newOppositeLength, nextWptDir);
        } else {
            newAcftX = getNewXNonTriangularPosition(nextWptDir, acft, speed);
            newAcftY = getNewYNonTriangularPosition(nextWptDir, acft, speed);
        }

        acft.setLatitude(newAcftX);
        acft.setLongitude(newAcftY);
        aircraftsDirectionIndication(acft);

        if (timerDifference >= 3000) {
            acft.getRouteTrail().add(new Point((int) newAcftX, (int) newAcftY));

        }

        if (checkWaypointPassed(acft, nextWpt, speed, nextWptDir)) {
            if (acft.getActualRoute().getRoutePoints().size() > 0) {
                acft.getActualRoute().getRoutePoints().remove(0);
            }
        }
    }

    private void aircraftsDirectionIndication(Aircraft aircraft)
            throws EmptyRouteException, WaypointNotLoadedException {
        int directionLineLength = 30;
        Waypoint nextWpt = getNextRouteWaypoint(aircraft, getAircraftRouteType(aircraft));
        NextWaypointDirectionEnum nextWptDir = getNextWaypointDirection(aircraft, nextWpt);
        double oppositeLength = calculateOppositeLength(aircraft, nextWpt, nextWptDir);
        double adjacentLength = calculateAdjacentLength(aircraft, nextWpt, nextWptDir);
        double hypotenuse = calculateHypotenuseLength(oppositeLength, adjacentLength);
        double newOppositeLength = calculateNewOppositeLength(hypotenuse, oppositeLength, directionLineLength);
        double newAdjacentLength = calculateNewAdjacentLength(hypotenuse, newOppositeLength, directionLineLength);
        double dirLineX = getNewXPosition(nextWpt, newAdjacentLength, nextWptDir);
        double dirLineY = getNewYPosition(nextWpt, newOppositeLength, nextWptDir);
        aircraft.getDirectionLine().x = (int) dirLineX;
        aircraft.getDirectionLine().y = (int) dirLineY;
    }

    /**
     * Zrýchlenie lietadla po 1-5 KTAS až kým nedosiahne svoju maximálnu povolenú rýchlosť letu
     *
     * @param acft Lietadlo, ktorého rýchlosť sa má zvyšovať.
     */
    private void accelerateAircraft(Aircraft acft) {
        int actualAirSpeed = acft.getActualAirSpeed();
        if (actualAirSpeed < acft.getFinalAirSpeed()) {
            acft.setActualAirSpeed(actualAirSpeed + (new Random()).nextInt(5) + 1);
        } else {
            acft.setActualAirSpeed(actualAirSpeed - (new Random()).nextInt(5) + 1);
        }
    }

    /**
     * Stúpanie lietadla po 1-5 stopách až kým nedosiahne svoju maximálnu povolenú výšku letu
     *
     * @param acft Lietadlo, ktorého letová hladina má stúpať.
     */
    private void climbAircraft(Aircraft acft) {
        int actualFlightLevel = acft.getActualFlightLevel();
        if (actualFlightLevel < acft.getFinalFlightLevel()
                && acft.getActualAirSpeed() > 100) {
            acft.setActualFlightLevel(actualFlightLevel + (new Random()).nextInt(5) + 1);
        }
        if (actualFlightLevel > acft.getFinalFlightLevel()) {
            acft.setActualFlightLevel(actualFlightLevel - (new Random()).nextInt(5) + 1);
        }
    }

    /**
     * Získa objekt letiska destinácie podľa ICAO kódu, ktorý má lietadlo zapísaný vo svojom pláne.
     *
     * @param aircraft Objekt lietadla, na ktorom sa destinácia vyhľadáva
     *
     * @return Letisko ako {@code Airport} objekt
     */
    private Airport getDestinationAirport(Aircraft aircraft) {
        Airport airport = null;
        for (Airport arpt : connectedAirports) {
            String airportICAO = arpt.getIcaoCode();
            if (aircraft.getArrAirport().equals(airportICAO)) {
                airport = arpt;
            }
        }
        return airport;
    }

    /**
     * Predá lietadlo po odchode z riadenej oblasti jedného letiska do riadenej oblasti druhého letiska, kde má určené pristátie.
     *
     * @param aircraft Lietadlo, ktoré sa má predať príletovému radaru.
     */
    private synchronized void handOffAircraft(Aircraft aircraft)
            throws EmptyRouteException, WaypointNotLoadedException {
        if (!aircraft.getArrAirport().equals(requestedArptIcao.toUpperCase())) {
            System.out.println("Mažem lietadlo...");
            globalGeneratedAircraft.get(threadNumber).remove(aircraft);
        }
        for (int i = 0; i < connectedAirports.size(); i++) {
            Airport airport = connectedAirports.get(i);
            if (airport.getIcaoCode().equals(aircraft.getArrAirport())) {
                Waypoint firstSTARWpt = getNextRouteWaypoint(aircraft, AirportRouteTypesEnum.STAR);
                aircraft.setActualRoute(aircraft.getStarRoute());
                if (!aircraft.getArrAirport().equals(requestedArptIcao.toUpperCase())) {
                    aircraft.setLatitude(firstSTARWpt.getPixelCoordX() + (new Random()).nextInt(5));
                    aircraft.setLongitude(firstSTARWpt.getPixelCoordY() + (new Random()).nextInt(5));
                    aircraft.getActualRoute().getRoutePoints().remove(firstSTARWpt.getName());
                    globalGeneratedAircraft.get(i).add(aircraft);
                }
            }
        }
    }

    /**
     * Modifikuje zoznam lietadiel, ktoré sa nachádzajú na letisku a v lietadle, ktorého atribúty boli modifikované riadiacim v klientskej časti aktualizuje údaje na tie čo klient nastavil.
     *
     * @param aircraft Lietadlo, ktorého atribúty sa majú zmeniť.
     */
    private void modifyControlledAircraft(Aircraft aircraft) {
        for (Aircraft a : generatedAircraft) {
            if (a.getCallSign().equals(aircraft.getCallSign())
                    && a.getAircraftType() == aircraft.getAircraftType()) {
                a.setFinalAirSpeed(aircraft.getFinalAirSpeed());
                a.setFinalFlightLevel(aircraft.getFinalFlightLevel());
                a.getActualRoute().setRoutePoints(aircraft.getActualRoute().getRoutePoints());
                a.setClearedForDeparture(aircraft.isClearedForDeparture());
            }
        }
    }

}
