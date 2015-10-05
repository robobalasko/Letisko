package net.robobalasko.letiskoserv.data;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.robobalasko.letiskoserv.lietadla.Aircraft;
import net.robobalasko.letiskoserv.navigacia.Airport;

/**
 * Trieda definujúca hlavný letiskový server, ktorý
 * sa stará o načítanie xml súborov zo zložiek a potom
 * po jednom vytvára žiadajúcim klientom o priepojenie
 * nové vlákna, v ktorých s nimi komunikuje a obsluhuje riadenie.
 * 
 * @author rbalasko
 */
public class AirportServerService {
    
    /**
     * Súbor s aktuálnym prekladom.
     */
    private final ResourceBundle language;
    
    /**
     * Subor s globálnymi nastaveniami.
     */
    private final ResourceBundle settings;
    
    /**
     * Socket hlavného letiskového servera.
     */
    private final ServerSocket airportServer;
    
    /**
     * Načítavač dostupných xml súborov letísk.
     */
    private final AirportLoader airportLoader;
    
    /**
     * Zoznam pripojených letísk.
     */
    private final List<Airport> connectedAirports;
    
    /**
     * Zoznam vygenerovaných lietadiel na všetkých letiskách,
     * ktoré momentálne server spravuje.
     */
    private final List<List<Aircraft>> generatedAircraft;
    
    /**
     * Počet aktuálne pripojených klientov k serveru.
     */
    private int connectedClients;
    
    /**
     * Základný konštruktor nastavuje atribútom hodnoty, vytvorí socket pre
     * hlavný letiskový server a po úspešnom načítaní letísk zo zložky čaká
     * na pripojenie sa klientov.
     * 
     * @param xmlDir Zložka, v ktorej sa nachádzajú xml súbory letísk.
     * @param language Jazykový súbor s prekladmi.
     * 
     * @throws IOException Ak sa nepodarí vytvoriť nový socket pre server.
     */
    public AirportServerService(File xmlDir, ResourceBundle language) throws IOException {
        this.language = language;
        this.settings = ResourceBundle.getBundle("config/settings");
        this.connectedClients = 0;
        // Zapne server a vytvorí hlavný socket
        System.out.println(language.getString("server.starting"));
        airportServer = new ServerSocket(Integer.parseInt(settings.getString("settings.server_port")));
        System.out.println(airportServer.getInetAddress().toString());
        // Inicializuje triedu, ktorá číta letiská
        System.out.println(language.getString("server.welcome"));
        this.airportLoader = new AirportLoader(xmlDir);
        // Inicializuje hlavný zoznam pripojených letísk
        this.connectedAirports = new LinkedList<Airport>();
        // Inicializuje hlavný zoznam lietadiel na letiskách
        this.generatedAircraft = new LinkedList<List<Aircraft>>();
        // Čaká na pripájanie klientov
        System.out.println(language.getString("server.waiting_for_clients"));
        acceptClients();
    }
    
    /**
     * Akceptuje klientov žiadajúcich o pripojenie
     * a vytvára pre nich samostatné vlákna, v ktorým
     * počas komunikácie so serverom bežia
     * 
     * @throws IOException Ak nie je možné vytvoriť nové vlákno pre klienta
     */
    private void acceptClients() throws IOException {
        List<String> availAirports = airportLoader.listAvailAirports();
        while (true) {
            generatedAircraft.add(new LinkedList<Aircraft>());
            AirportServerServiceThread arptServThread
                    = new AirportServerServiceThread(
                            airportServer.accept(),
                            availAirports,
                            language,
                            settings,
                            connectedClients,
                            airportLoader,
                            generatedAircraft,
                            connectedAirports);
            connectedClients++;
            arptServThread.start();
            Logger.getLogger(AirportServerService.class.getName()).log(Level.INFO,
                    "The server has started a new thread communicating with a client nr. {0}", connectedClients);
        }
    }

}
