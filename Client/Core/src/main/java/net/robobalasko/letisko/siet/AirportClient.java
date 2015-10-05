package net.robobalasko.letisko.siet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.robobalasko.letiskoserv.data.AirportServiceProtocol;
import net.robobalasko.letiskoserv.lietadla.Aircraft;
import net.robobalasko.letiskoserv.navigacia.Airport;

/**
 * Trieda, ktorá pre klientskú časť radaru zabezpečuje komunikáciu so serverom.
 *
 * @author rbalasko
 */
public final class AirportClient {

    /**
     * Socket, cez ktorý klient komunikuje so serverom.
     */
    private final Socket socket;

    /**
     * Protokol komunikácie so serverom.
     */
    private final AirportServiceProtocol protocol;

    /**
     * Prichádzajúce dáta zo strany servera.
     */
    private final ObjectInputStream incomingData;

    /**
     * Odchádzajúce dáta k serveru.
     */
    private final ObjectOutputStream outgoingData;

    /**
     * Hneď pri vytváraní objektu {@code AirportClient} sa konštruktor pokúša pripojiť k serveru.
     *
     * @param hostName Adresa servera, ku ktorému sa má klient pripojiť.
     * @param port Port, na ktorom server počúva.
     *
     * @throws IOException Vyhodená podmienka, ak sa spojenie nepodarí.
     */
    public AirportClient(String hostName, int port) throws IOException {
        this.socket = new Socket(hostName, port);
        this.protocol = new AirportServiceProtocol();
        this.outgoingData = new ObjectOutputStream(socket.getOutputStream());
        this.incomingData = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Vyžiada si od servera zoznam dostupných letísk, na ktoré sa môžu klienti pripájať.
     *
     * Server odošle klientovy spájaný zoznam, v ktorom sa nachádzajú
     * ICAO kódy všetkých letísk, ktoré momentálne na letisku nikto neobsadzuje.
     * Na jednom letisku môže byť v jednom momente pripojený len jeden riadiaci.
     *
     * @return Spájaný zoznam ICAO kódov.
     */
    public List<String> requestAirportsList() {
        List<String> airports = null;
        try {
            Integer requestFlag = AirportServiceProtocol.SEND_AIRPRT_LIST;
            outgoingData.writeObject(requestFlag);
            airports = (List<String>) incomingData.readObject();
        } catch (IOException ex) {
            Logger.getLogger(AirportClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AirportClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return airports;
    }

    /**
     * Odosiela serveru aktuálny rozmer obrazovky radaru, na ktorej sa budú
     * vykresľovať všetky navigačné objekty aj spolu s lietadlami a ich trasami.
     *
     * Údaj server potrebuje pre prepočet skutočných GPS dát všetkých
     * bodov na letisku a lietadiel, ktoré sa na letisku nachádzajú,
     * na pixelové súradnice pre obrazovku.
     *
     * @param screenWidth Šírka aktuálneho zobrazenia na radare.
     * @param screenHeight Výška aktuálneho zobrazenia na radare.
     */
    public void sendScreenSize(int screenWidth, int screenHeight) {
        try {
            Integer requestFlag = AirportServiceProtocol.SENT_GUISCR_DATA;
            outgoingData.writeObject(requestFlag);
            outgoingData.writeObject(screenWidth);
            outgoingData.writeObject(screenHeight);
        } catch (IOException ex) {
            Logger.getLogger(AirportClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Vyžiada si od servera objekt, ktorý obsahuje dáta načítané z .xml súboru
     * daného letiska, spolu s údajmi, ktoré sú už prepočítané
     * a vhodné na zobrazenie na radarovej obrazovke.
     *
     * @param airportIcaoCode ICAO kód letiska, ktoré klient žiada.
     * @param screenWidth Šírka aktuálneho zobrazenia na radare.
     * @param screenHeight Výška aktuálneho zobrazenia na radare.
     *
     * @return Načítané dáta letiska vo forme {@code Airport} objektu.
     */
    public Airport requestAirportData(String airportIcaoCode, int screenWidth, int screenHeight) {
        Airport requestedAirport = null;

        sendScreenSize(screenWidth, screenHeight);
        try {
            Integer requestFlag = AirportServiceProtocol.SEND_AIRPRT_DATA;
            outgoingData.writeObject(requestFlag);
            outgoingData.writeObject(airportIcaoCode);
            requestedAirport = (Airport) incomingData.readObject();
        } catch (IOException ex) {
            Logger.getLogger(AirportClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AirportClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return requestedAirport;
    }

    /**
     * Vyžiada od servera aktuálny zoznam lietadiel, ktoré sa na riadenom
     * letisku nachádzajú v danom momente na svojich pozíciách (v lete, na letisku,...).
     *
     * Server tento zoznam odosiela klientovi periodicky
     * každú sekundu hneď potom ako prepočíta pre všetky
     * lietadlá ich aktuálne pozície, príp. priloží do zoznamu nové lietadlo.
     *
     * @return Spájaný zoznam objektov {@code Aircraft}.
     */
    public List<Aircraft> requestAircraftsData() {
        List<Aircraft> generatedAircraft = null;
        try {
            Integer requestFlag = AirportServiceProtocol.SEND_AIRCFT_DATA;
            outgoingData.writeObject(requestFlag);
            generatedAircraft = (List<Aircraft>) incomingData.readObject();
        } catch (IOException ex) {
            Logger.getLogger(AirportClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AirportClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return generatedAircraft;
    }

    /**
     * Odosiela serveru zoznam lietadiel, ktoré boli riadiacim
     * počas ich letu modifikované, aby server mohol ďalej
     * prepočítavať dáta berúc ohľad na tieto nové modifikované dáta.
     *
     * Po každom odoslaní metóda zoznam modifikovaných lietadiel vymaže,
     * pretože tento sa odosiela serveru periodicky každú sekundu nezávisle
     * od toho, či sa v ňom nejaké modifikované lietadlo nachádza.
     *
     * @param modifiedAircraft Spájaný zoznam lietadiel, ktoré riadiaci modifikoval.
     */
    public void sendModifiedAircraft(List<Aircraft> modifiedAircraft) {
        try {
            Integer requestFlag = AirportServiceProtocol.RCVD_MODACFT_DATA;
            outgoingData.writeObject(requestFlag);
            outgoingData.writeObject(modifiedAircraft);
            outgoingData.reset();
            modifiedAircraft.clear();
        } catch (IOException ex) {
            Logger.getLogger(AirportClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Odosiela serveru žiadosť o ukončenie komunikácie s klientom.
     *
     * @return True, ak sa podarilo ukončiť komunikáciu.
     */
    public boolean endServerCommunication() {
        try {
            Integer requestFlag = AirportServiceProtocol.END_COM;
            outgoingData.writeObject(requestFlag);
            if ((Integer) incomingData.readObject() == AirportServiceProtocol.END_COM) {
                protocol.setState(AirportServiceProtocol.END_COM);
                outgoingData.close();
                incomingData.close();
                socket.close();
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(AirportClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AirportClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Vráti objekt s rotokolom medzi serverom a klientom.
     * 
     * @return Objekt protokolu.
     */
    public AirportServiceProtocol getProtocol() {
        return protocol;
    }

}
